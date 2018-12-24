package com.flemmli97.tenshilib.common.world.structure;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.api.config.IConfigSerializable;
import com.flemmli97.tenshilib.common.events.StructureGenerateEvent;
import com.google.common.collect.Lists;

import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class Structure implements IConfigSerializable<Structure>{
	
	private ResourceLocation id;
	private ResourceLocation[] startingStructures;
	private int frequency, yOffset, minDist;
	private int[] dimensions;
	private LocationType type;
	private GenerationType genType;
	private List<Biome> biomes = Lists.newLinkedList();
	private List<Biome> biomesRaw = Lists.newLinkedList();
	private List<Type> biomesTypes = Lists.newLinkedList();

	/**
	 * Start of a structure. Structures are located under "./assets/MODID/structures/". Currently only .nbt supported. 
	 * @param id The id of the whole structure. Translation key is "structure."+id.
	 * @param starting Structures structure pieces eligible for the first placement, if null the id will be picked.
	 * @param frequency Spawnrate of structure
	 * @param type If the structure spawns in air, ground, underground.
	 * @param genType See {@link GenerationType}
	 * @param dimID Dimensions this structure can spawn in
	 * @param yOffset Offset of the height during spawn
	 * @param biomeList Eligible biomes for the structure
	 * @param biomeTypes Eligible biomes types for the structure
	 */
	public Structure(ResourceLocation id, @Nullable ResourceLocation[] startingStructures, int frequency, int minDist, LocationType type, GenerationType genType, int[] dimID, int yOffset,List<Biome> biomeList, List<Type> biomeTypes)
	{
		this.id=id;
		if(startingStructures==null)
			startingStructures = new ResourceLocation[] {id};
		this.startingStructures=startingStructures;
		this.frequency=frequency;
		this.minDist=minDist;
		this.type=type;
		this.dimensions=dimID;
		this.yOffset=yOffset;
		this.biomesTypes=biomeTypes;
		this.biomesRaw=biomeList;
		this.biomes=biomeList;
		this.genType=genType;
		for(Type biomeType : biomeTypes)
		{
			for(Biome biome : BiomeDictionary.getBiomes(biomeType))
				this.biomes.add(biome);
		}
		for(ResourceLocation res : startingStructures)
			if(StructureLoader.getSchematic(res)==null)
				throw new NullPointerException("Schematic for structure ["+res.toString()+"] couldn't be loaded");
	}
	
	protected Structure(ResourceLocation id, String[] startingStructures, int frequency, int minDist, LocationType type, GenerationType genType, int[] dimID, int yOffset,List<Biome> biomeList, List<Type> biomeTypes)
	{
		this(id, fromStringArray(startingStructures), frequency, minDist, type, genType, dimID, yOffset, biomeList, biomeTypes);
	}
	
	private static ResourceLocation[] fromStringArray(String[] arr)
	{
		ResourceLocation[] res = new ResourceLocation[arr.length];
		for(int i = 0; i < res.length; i++)
			res[i]=new ResourceLocation(arr[i]);
		return res;
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
		if(this.frequency>0 && rightDimension && random.nextInt(this.frequency)==0 && world.getBiomeProvider().areBiomesViable(chunkX, chunkZ, 8, this.biomes))
		{
			int x = chunkX * 16 + random.nextInt(16)+8;
			int z = chunkZ * 16 + random.nextInt(16)+8;
			int y = Math.max(world.getSeaLevel(), world.getHeight(x, z)+this.yOffset);
			if(this.type!=LocationType.GROUND)
				y=random.nextInt(this.type.randomization())+this.type.minHeight();
			StructureMap structureMap = StructureMap.get(world);
			BlockPos pos = new BlockPos(x,y,z);
			StructureBase nearest = structureMap.getNearestStructure(this.id, pos, world);
			if((nearest!=null && pos.distanceSq(nearest.getPos())<(this.minDist*this.minDist)) || MinecraftForge.EVENT_BUS.post(new StructureGenerateEvent(this, pos, world)))
				return;
			Rotation rot = Rotation.values()[random.nextInt(Rotation.values().length)];
			Mirror mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
			ResourceLocation randomStart = this.startingStructures[random.nextInt(this.startingStructures.length)];		
			structureMap.initStructure(new StructureBase(this.id, randomStart, random, pos, rot, mirror, this.genType));
		}
	}
	
	public ResourceLocation structureName()
	{
		return this.id;
	}

	@Override
	public Structure config(Configuration config, Structure old, String configCategory) {
		ConfigCategory cat = config.getCategory(configCategory);
		cat.setLanguageKey("structures."+this.id);
		this.startingStructures=fromStringArray(config.getStringList("Starting Structures", configCategory, this.toStringList(this.startingStructures), "Schematic names of potential starting structures. If empty will use the structure id"));
		this.frequency=config.get(configCategory, "Frequency", this.frequency, "Structure will spawn with 1/x probability in a chunk").getInt();
		this.minDist=config.get(configCategory, "Min Distance", this.minDist, "Minimum distance to other structures with the same id").getInt();
		this.type=LocationType.valueOf(config.get("Location Type", configCategory, this.type.toString(), "Spawn height")
				.setValidValues(toStringList(LocationType.values())).getString());
		this.dimensions=this.fromString(config.getStringList("Dimensions", configCategory, this.toStringList(this.dimensions), "List of whitelisted dimension ids"));
		this.yOffset=config.get(configCategory, "Y-Offset", this.yOffset, "An y offset during generation").getInt();
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
		return this;
	}
	
	private String[] toStringList(int[] ts)
	{
		String[] arr = new String[ts.length];
		for(int i = 0; i < ts.length; i++)
			arr[i]=""+ts[i];
		return arr;
	}
	
	private <T> String[] toStringList(T[] ts)
	{
		String[] arr = new String[ts.length];
		for(int i = 0; i < ts.length; i++)
			arr[i]=ts[i].toString();
		return arr;
	}
	
	private int[] fromString(String[] s) {
		int[] arr = new int[s.length];
		for(int i = 0; i < s.length; i++)
			arr[i]=Integer.parseInt(s[i]);
		return arr;
	}
	
	public String toString()
	{
		return "Structure:[Id:"+this.id+",Freq:"+this.frequency+",MinDist:"+this.minDist+",LocationType:"+this.type+"]";
	}
}

