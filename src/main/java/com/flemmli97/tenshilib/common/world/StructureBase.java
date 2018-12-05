package com.flemmli97.tenshilib.common.world;

import java.util.List;
import java.util.Random;

import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;
import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;

/**
 * Contains data about a generated structure.
 */
public class StructureBase{

	private List<StructureBoundingBox> structureParts=Lists.newArrayList();
	private List<BlockPos> structurePieceBlocks = Lists.newArrayList();
	private List<ChunkPos> chunks;
	private BlockPos structurePos;
	private ResourceLocation structureID, schematicID;
	private boolean isGround;
	private Rotation rot;
	private Mirror mirror;
	
	public StructureBase(ResourceLocation id, ResourceLocation schematicID, BlockPos pos, Rotation rot, Mirror mirror, boolean ground)
	{
		Schematic schematic = StructureLoader.getSchematic(schematicID);
		//Size of the starting structure
		StructureBoundingBox startingBox = StructureBase.getBox(schematic, rot, pos);
		this.structureParts.add(startingBox);
		this.structurePos=pos;
		this.structureID=id;
		this.schematicID=schematicID;
		this.isGround=ground;
		this.rot=rot;
		this.mirror=mirror;
		this.chunks=StructureBase.calculateChunks(startingBox);
	}
	
	public static StructureBoundingBox getBox(Schematic schem, Rotation rot, BlockPos pos)
	{
		BlockPos corner = new BlockPos(schem.x,schem.y,schem.z);
		if(rot==Rotation.CLOCKWISE_90||rot==Rotation.COUNTERCLOCKWISE_90)
			corner = new BlockPos(schem.z,schem.y,schem.x);
		return StructureBoundingBox.createProper
				(corner.getX(), pos.getY(), corner.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}
	
	public StructureBase(NBTTagCompound compound)
	{
		this.readFromNBT(compound);
	}
	
	public boolean isInside(Vec3i vec)
	{
		for(StructureBoundingBox box : this.structureParts)
		{
			if(box.isVecInside(vec))
				return true;
		}
		return false;
	}
	
	public void add(TileStructurePiece piece)
	{
		this.structurePieceBlocks.add(piece.getPos());
	}
	
	//Since we use the passed chunk this time the positive chunks should be loaded!
	public synchronized void process(World world, Random rand, int chunkX, int chunkZ)
	{
		Schematic schem = StructureLoader.getSchematic(this.schematicID);
		ChunkPos pos = new ChunkPos(chunkX, chunkZ);
		if(this.chunks.contains(pos))
		{
			schem.generate(world, this.structurePos, this.rot, this.mirror, 
					this.isGround, StructureBase.getChunk(pos.x, pos.z), this);
			this.chunks.remove(pos);
		}
		List<BlockPos> list = Lists.newArrayList();
		for(BlockPos blockPos : this.structurePieceBlocks)
		{
			TileEntity tile = world.getTileEntity(blockPos);
			if(tile instanceof TileStructurePiece)
			{
				TileStructurePiece piece = (TileStructurePiece) tile;
				piece.initStructure(rand);
				if(piece.hasChunk(pos))
				{
					piece.runBlock(rand, piece.getPos(), getChunk(pos.x, pos.z), this);
					if(piece.finish(pos))
						list.add(blockPos);
				}
			}
		}
		list.forEach(blockPos -> this.structurePieceBlocks.remove(blockPos));
	}
	
	public static List<ChunkPos> calculateChunks(StructureBoundingBox box)
	{
		List<ChunkPos> list = Lists.newArrayList();
		//Test if its at the negative border and if so add the chunk it lies at too
		int chunkX1 = box.minX>>4+(box.minX%16==0?-1:0);
		int chunkZ1 = box.minZ>>4+(box.minZ%16==0?-1:0);
		int chunkX2 = box.maxX>>4;
		int chunkZ2 = box.maxZ>>4;
		for(int x = chunkX1; x<=chunkX2;x++)
			for(int z = chunkZ1; z<=chunkZ2; z++)
				list.add(new ChunkPos(x, z));
		return list;
	}
	
	public static StructureBoundingBox getChunk(BlockPos pos)
	{
		return getChunk(pos.getX()>>4, pos.getZ()>>4);
	}
	
	public static StructureBoundingBox getChunk(int chunkX, int chunkZ)
	{
		int x=chunkX*16;
		int z=chunkZ*16;
		//With the negative check add 1 to the box should be fine. i hope
		return new StructureBoundingBox(x+1,0,z+1,x+17,255,z+17);
	}
	
	public ResourceLocation getStructureId()
	{
		return this.structureID;
	}
	
	public BlockPos getPos()
	{
		return this.structurePos;
	}
	
	public void expand(StructureBoundingBox box)
	{
		this.structureParts.add(box);
	}
	
	public void readFromNBT(NBTTagCompound compound)
	{
		int[] arr = compound.getIntArray("Pos");
		this.structurePos = new BlockPos(arr[0], arr[1], arr[2]);
		NBTTagList list = compound.getTagList("Parts", Constants.NBT.TAG_COMPOUND);
		list.forEach(nbt -> {
			this.structureParts.add(new StructureBoundingBox(((NBTTagIntArray)nbt).getIntArray()));
		});
		this.structureID=new ResourceLocation(compound.getString("ID"));
		this.schematicID=new ResourceLocation(compound.getString("Schematic"));
		NBTTagList list2 = compound.getTagList("StructureChunks", Constants.NBT.TAG_INT_ARRAY);
		for(int i = 0; i < list2.tagCount(); i++)
		{
			int[] arr2 = list2.getIntArrayAt(i);
			this.chunks.add(new ChunkPos(arr2[0], arr2[1]));
		}
		NBTTagList list3 = compound.getTagList("StructurePieceBlocks", Constants.NBT.TAG_INT_ARRAY);
		for(int i = 0; i < list3.tagCount(); i++)
		{
			int[] arr3 = list3.getIntArrayAt(i);
			this.structurePieceBlocks.add(new BlockPos(arr3[0], arr3[1], arr3[2]));
		}
		this.mirror = Mirror.valueOf(compound.getString("Mirror"));
		this.rot = Rotation.valueOf(compound.getString("Rotation"));
		this.isGround = compound.getBoolean("ReplaceGround");
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setIntArray("Pos", new int[] {this.structurePos.getX(), this.structurePos.getY(), this.structurePos.getZ()});
		NBTTagList list = new NBTTagList();
		for(StructureBoundingBox b : this.structureParts)
			list.appendTag(b.toNBTTagIntArray());
		compound.setTag("Parts", list);
		compound.setString("ID", this.structureID.toString());
		compound.setString("Schematic", this.schematicID.toString());
		NBTTagList list2 = new NBTTagList();
		for(ChunkPos pos : this.chunks)
			list2.appendTag(new NBTTagIntArray(new int[] {pos.x, pos.z}));
		compound.setTag("StructureChunks", list2);
		NBTTagList list3 = new NBTTagList();
		for(BlockPos pos : this.structurePieceBlocks)
			list3.appendTag(new NBTTagIntArray(new int[] {pos.getX(), pos.getY(), pos.getZ()}));
		compound.setTag("StructurePieceBlocks", list3);
		compound.setString("Mirror", this.mirror.toString());
		compound.setString("Rotation", this.rot.toString());
		compound.setBoolean("ReplaceGround", this.isGround);
		return compound;
	}
}
