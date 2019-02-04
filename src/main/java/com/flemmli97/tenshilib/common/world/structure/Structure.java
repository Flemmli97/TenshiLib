package com.flemmli97.tenshilib.common.world.structure;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.api.config.IConfigSerializable;
import com.flemmli97.tenshilib.common.config.ConfigUtils;
import com.flemmli97.tenshilib.common.events.StructureGenerateEvent;
import com.flemmli97.tenshilib.common.javahelper.ArrayUtils;
import com.flemmli97.tenshilib.common.javahelper.ObjectConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class Structure implements IConfigSerializable<Structure>{
	
	private ResourceLocation id;
	private ResourceLocation[] startingStructures;
	private int frequency, yOffset, minDist, maxParts;
	private int[] dimensions;
	private LocationType type;
	private GenerationType genType;
	private List<Biome> biomes = Lists.newArrayList();
	/**Used for the config*/
	private List<Biome> biomesRaw = Lists.newArrayList();
	private List<Type> biomesTypes = Lists.newArrayList();
	private boolean preventOtherSpawn;
	private Map<EnumCreatureType,Set<Biome.SpawnListEntry>> spawns = Maps.newHashMap();
	/**
	 * Start of a structure. Structures are located under "./assets/MODID/structures/". Currently only .nbt supported. Should be used for structures which are trackable and/or bigger than a chunk.
	 * @param id The id of the whole structure. Translation key is "structure."+id.
	 * @param startingStructures Structures structure pieces eligible for the first placement, if null the id will be picked.
	 * @param frequency Spawnrate of structure
	 * @param minDist
	 * @param maxParts
	 * @param type If the structure spawns in air, ground, underground.
	 * @param genType See {@link GenerationType}
	 * @param dimID Dimensions this structure can spawn in
	 * @param yOffset Offset of the height during spawn
	 * @param biomeList Eligible biomes for the structure
	 * @param biomeTypes Eligible biomes types for the structure
	 */
	public Structure(ResourceLocation id, @Nullable ResourceLocation[] startingStructures, int frequency, int minDist, int maxParts, LocationType type, GenerationType genType, int[] dimID, int yOffset, boolean preventOtherMobSpawn)
	{
		this.id=id;
		if(startingStructures==null)
			startingStructures = new ResourceLocation[] {id};
		this.startingStructures=startingStructures;
		this.frequency=frequency;
		this.minDist=minDist;
		this.maxParts=maxParts;
		this.type=type;
		this.genType=genType;
		this.dimensions=dimID;
		this.yOffset=yOffset;
		this.preventOtherSpawn=preventOtherMobSpawn;
		for(ResourceLocation res : startingStructures)
			if(StructureLoader.getSchematic(res)==null)
				throw new NullPointerException("Schematic for structure ["+res.toString()+"] couldn't be loaded");
	}
	
	public Structure addBiome(Set<Biome> biomes)
	{
		biomes.forEach(biome->{
			if(!this.biomesRaw.contains(biome))
			{
				this.biomesRaw.add(biome);
				this.biomes.add(biome);
			}
		});
		return this;
	}
	
	public Structure addBiomeType(Set<Type> biomeTypes)
	{
		for(Type type : biomeTypes)
		{
			for(Biome biome : BiomeDictionary.getBiomes(type))
				if(!this.biomes.contains(biome))
					this.biomes.add(biome);
			if(!this.biomesTypes.contains(type))
				this.biomesTypes.add(type);
		}
		return this;
	}
	
	public Structure addMobSpawn(EnumCreatureType type, Set<Biome.SpawnListEntry> biome)
	{
		spawns.merge(type, biome, (old, newSet)->{old.addAll(newSet);return old;});
		return this;
	}
	
	public void startStructure(World world, int chunkX, int chunkZ, Random random)
	{
		boolean rightDimension = false;
		for(int id : this.dimensions)
			if(world.provider.getDimension()==id)
			{
				rightDimension=true;
				break;
			}
		if(this.frequency>0 && rightDimension && random.nextInt(this.frequency)==0 && world.getBiomeProvider().areBiomesViable(chunkX, chunkZ, 0, this.biomes))
		{
			int x = chunkX * 16 + random.nextInt(16)+8;
			int z = chunkZ * 16 + random.nextInt(16)+8;
			int y = Math.max(world.getSeaLevel(), world.getHeight(x, z));
			if(this.type!=LocationType.GROUND)
				y=random.nextInt(this.type.randomization())+this.type.minHeight();
			y+=this.yOffset;
			StructureMap structureMap = StructureMap.get(world);
			BlockPos pos = new BlockPos(x,y,z);
			StructureBase nearest = structureMap.getNearestStructure(this.id, pos, world);
			if((nearest!=null && pos.distanceSq(nearest.getPos())<(this.minDist*this.minDist)) || MinecraftForge.TERRAIN_GEN_BUS.post(new StructureGenerateEvent(this, pos, world)))
				return;
			Rotation rot = Rotation.values()[random.nextInt(Rotation.values().length)];
			Mirror mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
			ResourceLocation randomStart = this.startingStructures[random.nextInt(this.startingStructures.length)];		
			structureMap.initStructure(new StructureBase(this.id, randomStart, random, pos, rot, mirror, this.genType, this.maxParts));
		}
	}
	
	public ResourceLocation structureName()
	{
		return this.id;
	}
	
	public int maxParts() 
	{
		return this.maxParts;
	}
	
	public Set<Biome.SpawnListEntry> getSpawnList(EnumCreatureType type)
	{
		Set<Biome.SpawnListEntry> set = this.spawns.get(type);
		return set!=null?set:Sets.newHashSet();
	}
	
	public boolean preventOtherMobSpawn()
	{
		return this.preventOtherSpawn;
	}

	@Override
	public Structure config(Configuration config, String configCategory) {
		configCategory+="."+this.id;
		ConfigCategory cat = config.getCategory(configCategory);
		cat.setLanguageKey("structures."+this.id);
		this.startingStructures=ArrayUtils.arrayConverter(
				config.getStringList("Starting Structures", configCategory, ArrayUtils.arrayToStringArr(this.startingStructures), "Schematic names of potential starting structures. If empty will use the structure id"), 
				new ObjectConverter<String, ResourceLocation>() {
					@Override
					public ResourceLocation convertFrom(String t) {return new ResourceLocation(t);}}, ResourceLocation.class, true);
		if(this.startingStructures==null)
			this.startingStructures=new ResourceLocation[] {this.id};
		this.frequency=config.get(configCategory, "Frequency", this.frequency, "Structure will spawn with 1/x probability in a chunk").getInt();
		this.yOffset=config.get(configCategory, "Y-Offset", this.yOffset, "An y offset during generation").getInt();
		this.minDist=config.get(configCategory, "Min Distance", this.minDist, "Minimum distance to other structures with the same id").getInt();
		this.maxParts=config.get(configCategory, "Max Parts", this.maxParts, "Maximum of subpieces this structure can have").getInt();
		this.dimensions=config.get(configCategory, "Dimensions", this.dimensions, "List of whitelisted dimension ids").getIntList();
		this.type=ConfigUtils.getEnumVal(config, configCategory, "Location Type", "Spawn height", this.type);
		this.genType=ConfigUtils.getEnumVal(config, configCategory, "Generation Type", "How the the structure should be generated", this.genType);
		//Biomes
		String[] arr2 = new String[this.biomesRaw.size()];
		for(int i = 0; i < this.biomesRaw.size(); i++)
			arr2[i]=ForgeRegistries.BIOMES.getKey(this.biomesRaw.get(i)).toString();
		List<Biome> biomeList = Lists.newLinkedList();
		for(String s : config.getStringList("Biomes", configCategory, arr2, "Biomes eligible for this structure"))
		{
			Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(s));
			if(biome!=null)
				biomeList.add(biome);
		}
		this.biomes=biomeList;
		this.biomesRaw=biomeList;
		//Biome types
		List<Type> biomeTypes = Lists.newLinkedList();
		String[] arr3 = new String[this.biomesTypes.size()];
		for(int i = 0; i < this.biomesTypes.size(); i++)
			arr3[i]=this.biomesTypes.get(i).getName();
		for(String s : config.getStringList("Biomes Types", configCategory, arr3, "Biome Types eligible for this structure"))
		{
			biomeTypes.add(BiomeDictionary.Type.getType(s));
		}
		this.biomesTypes=biomeTypes;
		for(Type biomeType : biomeTypes)
		{
			for(Biome biome : BiomeDictionary.getBiomes(biomeType))
				this.biomes.add(biome);
		}
		this.preventOtherSpawn=config.getBoolean("Prevent MobSpawn", configCategory, this.preventOtherSpawn, "Prevent other mobspawn in this structure");
		for(EnumCreatureType type : EnumCreatureType.values())
		{
			Set<Biome.SpawnListEntry> set = this.spawns.getOrDefault(type, Sets.newHashSet());		
			String[] def = new String[set.size()];
			int i = 0;
			for(Biome.SpawnListEntry entry : set)
			{
				def[i] = spawnEntryString(entry);
				i++;
			}
			set.clear();
			for(String s : config.getStringList(type.toString(), configCategory+".spawns", def, "Syntax is: <Full Classname>;<minGroup>;<maxGroup>;<weight>"))
			{
				set.add(fromString(s));
			}
			if(!set.isEmpty())
				this.spawns.put(type, set);
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public static Biome.SpawnListEntry fromString(String s)
	{
		String[] subs = s.split(";");
		if(subs.length<4)
			return null;
		Class<?> clss=null;
		try {
			clss = Class.forName(subs[0]);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(clss==null || !EntityLivingBase.class.isAssignableFrom(clss))
			return null;
		return new Biome.SpawnListEntry((Class<? extends EntityLiving>) clss, Integer.parseInt(subs[1]),Integer.parseInt(subs[2]),Integer.parseInt(subs[3]));
	}
	
	public static String spawnEntryString(Biome.SpawnListEntry s)
	{
		return s.entityClass.getName()+";"+s.minGroupCount+";"+s.maxGroupCount+";"+s.itemWeight;
	}
	
	@Override
	public String toString()
	{
		return "Structure:[Id:"+this.id+",Freq:"+this.frequency+",MinDist:"+this.minDist+",LocationType:"+this.type+"]";
	}
}

