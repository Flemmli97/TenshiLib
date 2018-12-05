package com.flemmli97.tenshilib.common.world;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public class StructureMap extends WorldSavedData{

	private static final String id = "Structures";
	private Map<ResourceLocation, Long2ObjectMap<StructureBase>> map = Maps.newHashMap();
	private List<StructureBase> structureToGenerate = Lists.newArrayList();
	public StructureMap(String id) {
		super(id);
	}
	
	public StructureMap() {
		this(id);
	}
	
	public static StructureMap get(World world)
	{
		MapStorage storage = world.getMapStorage();
		StructureMap data = (StructureMap)storage.getOrLoadData(StructureMap.class, id);
		if (data == null)
		{
			data = new StructureMap();
			storage.setData(id, data);
		}
		return data;
	}

	public StructureBase getNearestStructure(ResourceLocation id, BlockPos pos, World world)
	{
		Long2ObjectMap<StructureBase> map = this.map.get(id);
		if(map!=null)
		{
			double dist = Double.MAX_VALUE;
			StructureBase nearest = null;
			for(StructureBase base : map.values())
			{
				double distTo = pos.distanceSq(base.getPos());
				if(distTo<dist)
				{
					nearest=base;
					dist=distTo;
				}
			}
			return nearest;
		}
		return null;
	}
	//Cache current structure
	private StructureBase base;
	public boolean isInside(ResourceLocation id, BlockPos pos)
	{
		if(id==null)
		{
			for(Long2ObjectMap<StructureBase> e : this.map.values())
			{
				for(StructureBase struc: e.values())
					if(struc.isInside(pos))
					{
						this.base=struc;
						return true;
					}
			}
			return false;
		}
		Long2ObjectMap<StructureBase> map = this.map.get(id);
		if(map!=null)
		{
			ObjectIterator<StructureBase> it = map.values().iterator();
			while(it.hasNext())
			{
				StructureBase base = it.next();
				if(base.isInside(pos))
				{
					this.base=base;
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	@Nullable
	public ResourceLocation currentStructure(BlockPos pos)
	{
		if(this.base==null)
			this.isInside(null, pos);
		if(this.base.isInside(pos))
			return this.base.getStructureId();
		this.base=null;
		return null;
	}
	
	public void initStructure(StructureBase base, int chunkX, int chunkZ)
	{
		Long2ObjectMap<StructureBase> chunkMap = this.map.get(base.getStructureId());
		if(chunkMap==null)
			chunkMap = new Long2ObjectOpenHashMap<StructureBase>(1024);
		if(!chunkMap.containsKey(ChunkPos.asLong(chunkX, chunkZ)))
			chunkMap.put(ChunkPos.asLong(chunkX, chunkZ), base);
		this.structureToGenerate.add(base);
		this.map.put(base.getStructureId(), chunkMap);
		this.markDirty();
	}
	
	public void removeStructureToProcess(StructureBase base)
	{
		this.structureToGenerate.remove(base);
		this.markDirty();
	}
	
	public void generate(World world, Random rand, int chunkX, int chunkZ)
	{
		for(StructureBase base : this.structureToGenerate)
		{
			base.process(world, rand, chunkX, chunkZ);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		for(String id : nbt.getKeySet())
		{
			Long2ObjectMap<StructureBase> map = new Long2ObjectOpenHashMap<StructureBase>(1024);
			NBTTagCompound compound = nbt.getCompoundTag(id);
			for(String chunkPos : compound.getKeySet())
			{
				map.put(Long.parseLong(chunkPos), new StructureBase(compound.getCompoundTag(chunkPos)));
			}
		}
		NBTTagList list = nbt.getTagList("IncompleteStructures", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < list.tagCount(); i++)
			this.structureToGenerate.add(new StructureBase(list.getCompoundTagAt(i)));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for(Entry<ResourceLocation, Long2ObjectMap<StructureBase>> entry: this.map.entrySet())
		{
			NBTTagCompound tag = new NBTTagCompound();
			for(Entry<Long, StructureBase> entry2: entry.getValue().entrySet())
			{
				tag.setTag(entry2.getKey().toString(), entry2.getValue().writeToNBT(new NBTTagCompound()));
			}
			compound.setTag(entry.getKey().toString(), tag);
		}
		NBTTagList list = new NBTTagList();
		for(StructureBase base : this.structureToGenerate)
			list.appendTag(base.writeToNBT(new NBTTagCompound()));
		compound.setTag("IncompleteStructures", list);
		return compound;
	}
}
