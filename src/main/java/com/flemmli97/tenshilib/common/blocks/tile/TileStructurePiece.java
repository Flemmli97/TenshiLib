package com.flemmli97.tenshilib.common.blocks.tile;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.common.world.Schematic;
import com.flemmli97.tenshilib.common.world.StructureBase;
import com.flemmli97.tenshilib.common.world.StructureLoader;
import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;

public class TileStructurePiece extends TileEntity{

	private ResourceLocation[] structureName;
	private Mirror mirror=Mirror.NONE;
	private Rotation rot = Rotation.NONE;
	private boolean replaceGround;
	
	//Will be initialized at TileStructurePiece.initStructure
	private ResourceLocation structureToGen;
	private List<ChunkPos> structureChunks = Lists.newArrayList();
	private boolean initialized;
	
	public TileStructurePiece() {}
	
	public void setStructureName(ResourceLocation[] res)
	{
		this.structureName=res;
	}
	
	@Override
	public void mirror(Mirror m)
	{
		this.mirror=m;
	}
	
	public Mirror currentMirror()
	{
		return this.mirror;
	}
	
	@Override
	public void rotate(Rotation rot)
	{
		this.rot=rot;
	}
	
	public Rotation currentrotation()
	{
		return this.rot;
	}
	
	public void setReplaceGround(boolean flag)
	{
		this.replaceGround=flag;
	}
	
	public ResourceLocation initStructure(Random rand)
	{
		if(!this.initialized)
		{
			if(this.structureToGen==null)
				this.structureToGen=this.structureName[rand.nextInt(this.structureName.length)];
			Schematic schematic = StructureLoader.getSchematic(this.structureToGen);
			this.structureChunks=StructureBase.calculateChunks(StructureBase.getBox(schematic, this.rot, this.pos));
			this.initialized=true;
		}
		return this.structureToGen;
	}
	
	public boolean hasChunk(ChunkPos pos)
	{
		return this.structureChunks.contains(pos);
	}
	
	public boolean finish(ChunkPos pos)
	{
		this.structureChunks.remove(pos);
		return this.structureChunks.isEmpty();
	}
	
	public void runBlock(Random rand, BlockPos pos, @Nullable StructureBoundingBox box,  @Nullable StructureBase base)
	{
		if(this.structureName!=null)
		{
			Schematic schematic = StructureLoader.getSchematic(this.initStructure(rand));
			if(schematic!=null)
			{
				schematic.generate(this.world, pos, this.rot, this.mirror, this.replaceGround, 
						box, base);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if(compound.hasKey("Structures"))
		{
			NBTTagList list = compound.getTagList("Structures", Constants.NBT.TAG_STRING);
			this.structureName = new ResourceLocation[list.tagCount()];
			for(int i = 0; i < list.tagCount(); i++)
				this.structureName[i] = new ResourceLocation(list.getStringTagAt(i));
		}
		this.mirror = Mirror.valueOf(compound.getString("Mirror"));
		this.rot = Rotation.valueOf(compound.getString("Rotation"));
		this.replaceGround = compound.getBoolean("ReplaceGround");
		
		this.initialized=compound.getBoolean("Initialized");
		if(compound.hasKey("StructureToGenerate"))
			this.structureToGen=new ResourceLocation(compound.getString("StructureToGenerate"));
		NBTTagList list2 = compound.getTagList("StructureChunks", Constants.NBT.TAG_INT_ARRAY);
		for(int i = 0; i < list2.tagCount(); i++)
		{
			int[] arr = list2.getIntArrayAt(i);
			this.structureChunks.add(new ChunkPos(arr[0], arr[1]));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if(this.structureName!=null)
		{
			NBTTagList list = new NBTTagList();
			for(ResourceLocation res : this.structureName)
				list.appendTag(new NBTTagString(res.toString()));
			compound.setTag("Structures", list);
		}
		compound.setString("Mirror", this.mirror.toString());
		compound.setString("Rotation", this.rot.toString());
		compound.setBoolean("ReplaceGround", this.replaceGround);
		
		if(this.structureToGen!=null)
			compound.setString("StructureToGenerate", this.structureToGen.toString());
		compound.setBoolean("Initialized", this.initialized);
		NBTTagList list = new NBTTagList();
		this.structureChunks.forEach(pos->list.appendTag(new NBTTagIntArray(new int[] {pos.x, pos.z})));
		compound.setTag("StructureChunks", list);
		return compound;	
	}
}
