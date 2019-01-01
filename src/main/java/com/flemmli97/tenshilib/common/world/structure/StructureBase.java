package com.flemmli97.tenshilib.common.world.structure;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
	
	private static final ResourceLocation structureTile = TileEntity.getKey(TileStructurePiece.class);
	
	private ResourceLocation structureID;
	private BlockPos structurePos;
	
	//Used for the inital structure piece
	private Mirror mirror=Mirror.NONE;
	private Rotation rot = Rotation.NONE;
	private GenerationType genType;
	private int maxSize;
	private List<StructureBoundingBox> boundingBoxes =Lists.newArrayList();
	private Set<StructurePiece> structurePieces = Sets.newHashSet();

	public StructureBase(ResourceLocation id, ResourceLocation schematicID, Random random, BlockPos pos, Rotation rot, Mirror mirror, GenerationType genType, int maxSize)
	{
		//Size of the starting structure
		this.structurePos=pos;
		this.structureID=id;
		this.rot=rot;
		this.mirror=mirror;
		this.genType=genType;
		StructurePiece start = start(schematicID, random, pos, rot, mirror, genType);
		if(start!=null)
			this.structurePieces.add(start);
	}
	
	public StructureBase(NBTTagCompound compound)
	{
		this.readFromNBT(compound);
	}
	
	private StructurePiece start(ResourceLocation schematicID, Random random, BlockPos pos, Rotation rot, Mirror mirror, GenerationType genType)
	{
		Schematic schematic = StructureLoader.getSchematic(schematicID);
		if(schematic==null)
		{
			TenshiLib.logger.error("Schematic {} could't be loaded", schematicID);
			return null;
		}
		//Check schematic for TileStructurePieces
		this.addParts(schematic, pos, random);
		return new StructurePiece(schematicID, mirror, rot, genType,  pos, this, random);
	}
	
	/**
	 * Add all StructurePieces read from the given schematic.
	 */
	protected void addParts(Schematic schematic, BlockPos pos, Random random)
	{
		schematic.getTileEntities().forEach(entry->{
			if(entry.getValue().getString("id").equals(structureTile.toString()))
			{
				NBTTagCompound compound = entry.getValue();
				BlockPos place = Schematic.transformPos(entry.getKey(), this.mirror, this.rot, new BlockPos(schematic.x, schematic.y
						, schematic.z)).add(pos);
				compound.setInteger("x", place.getX());
				compound.setInteger("y", place.getY());
				compound.setInteger("z", place.getZ());
				TileStructurePiece tile = new TileStructurePiece();
				tile.readFromNBT(compound);
				tile.initStructure(random, StructureBase.this);
			}
		});
	}
	
	protected boolean addStructurePiece(StructurePiece piece)
	{
		if(this.structurePieces.size()<this.maxSize || this.maxSize==-1)
			return this.structurePieces.add(piece);
		return false;
	}
	
	public Rotation getRot()
	{
		return this.rot;
	}
	
	public Mirror getMirror()
	{
		return this.mirror;
	}
	
	public GenerationType genType()
	{
		return this.genType;
	}
	
	public boolean isInside(Vec3i vec)
	{
		for(StructureBoundingBox box : this.boundingBoxes)
		{
			if(box.isVecInside(vec))
				return true;
		}
		return false;
	}
	
	public boolean intersects(StructureBoundingBox ssbb)
	{
		for(StructureBoundingBox box : this.boundingBoxes)
		{
			if(ssbb.intersectsWith(box))
				return true;
		}
		return false;
	}
	
	public List<StructureBoundingBox> getBoxes()
	{
		return ImmutableList.copyOf(this.boundingBoxes);
	}

	//Since we use the passed chunk this time the positive chunks should be loaded!
	public boolean process(World world, int chunkX, int chunkZ)
	{
		this.structurePieces.removeIf(piece->piece.generate(world, chunkX, chunkZ));
		return this.structurePieces.isEmpty();
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
		this.boundingBoxes.add(box);
	}
	
	public void readFromNBT(NBTTagCompound compound)
	{
		this.structureID = new ResourceLocation(compound.getString("ID"));
		int[] arr = compound.getIntArray("Pos");
		this.structurePos = new BlockPos(arr[0], arr[1], arr[2]);
		this.mirror = Mirror.valueOf(compound.getString("Mirror"));
		this.rot = Rotation.valueOf(compound.getString("Rotation"));
		this.genType = GenerationType.valueOf(compound.getString("GenerationType"));
		NBTTagList boundingBoxes = compound.getTagList("BoundingBoxes", Constants.NBT.TAG_INT_ARRAY);
		boundingBoxes.forEach(nbt -> {
			this.boundingBoxes.add(new StructureBoundingBox(((NBTTagIntArray)nbt).getIntArray()));
		});
		NBTTagList pieces = compound.getTagList("StructurePieces", Constants.NBT.TAG_COMPOUND);
		pieces.forEach(nbt -> {
			this.structurePieces.add(new StructurePiece((NBTTagCompound) nbt));
		});
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setString("ID", this.structureID.toString());
		compound.setIntArray("Pos", new int[] {this.structurePos.getX(), this.structurePos.getY(), this.structurePos.getZ()});
		compound.setString("Mirror", this.mirror.toString());
		compound.setString("Rotation", this.rot.toString());
		compound.setString("GenType", this.genType.toString());
		NBTTagList boundingBoxes = new NBTTagList();
		this.boundingBoxes.forEach(box->boundingBoxes.appendTag(box.toNBTTagIntArray()));
		compound.setTag("BoundingBoxes", boundingBoxes);
		NBTTagList pieces = new NBTTagList();
		this.structurePieces.forEach(piece->pieces.appendTag(piece.writeToNBT(new NBTTagCompound())));
		compound.setTag("StructurePieces", pieces);
		return compound;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.structurePos, this.structureID);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this==obj)
			return true;
		if(obj instanceof StructureBase)
		{
			StructureBase other = (StructureBase) obj;
			return this.structurePos.equals(other.structurePos)&&this.structureID.equals(other.structureID);
		}
		return false;
		
	}
	
	@Override
	public String toString()
	{
		return "Structure:"+this.structureID+" at BlockPos:["+this.structurePos + "] with BoundingBoxes:["+this.boundingBoxes+"]";
	}
	
	public static StructureBoundingBox getBox(Schematic schem, Rotation rot, BlockPos pos)
	{
		BlockPos corner = new BlockPos(schem.x,schem.y,schem.z);
		if(rot==Rotation.CLOCKWISE_90||rot==Rotation.COUNTERCLOCKWISE_90)
			corner = new BlockPos(schem.z,schem.y,schem.x);
		return StructureBoundingBox.createProper
				(corner.getX()+pos.getX(), corner.getY()+pos.getY(), corner.getZ()+pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}
	
	public static Set<ChunkPos> calculateChunks(StructureBoundingBox box)
	{
		Set<ChunkPos> set = Sets.newHashSet();
		//Test if its at the negative border and if so add the chunk it lies at too
		int chunkX1 = box.minX>>4+(box.minX%16==0?-1:0);
		int chunkZ1 = box.minZ>>4+(box.minZ%16==0?-1:0);
		int chunkX2 = box.maxX>>4;
		int chunkZ2 = box.maxZ>>4;
		for(int x = chunkX1; x<=chunkX2;x++)
			for(int z = chunkZ1; z<=chunkZ2; z++)
				set.add(new ChunkPos(x, z));
		return set;
	}
	
	public static StructureBoundingBox getChunk(BlockPos pos)
	{
		return getChunk(pos.getX()>>4, pos.getZ()>>4);
	}
	
	public static StructureBoundingBox getChunk(int chunkX, int chunkZ)
	{
		int x=chunkX*16+1;
		int z=chunkZ*16+1;
		//With the negative check add 1 to the box should be fine. i hope
		return new StructureBoundingBox(x,z,x+15,z+15);
	}
}

