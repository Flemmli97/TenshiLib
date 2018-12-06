package com.flemmli97.tenshilib.common.blocks.tile;

import java.util.Random;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.common.world.Position;
import com.flemmli97.tenshilib.common.world.Schematic;
import com.flemmli97.tenshilib.common.world.StructureBase;
import com.flemmli97.tenshilib.common.world.StructurePiece;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public class TileStructurePiece extends TileEntity{

	private ResourceLocation[] structureName;
	
	private StructurePiece piece;
	private Mirror mirror=Mirror.NONE;
	private Rotation rot = Rotation.NONE;
	private boolean replaceGround;
	private BlockPos offSet = BlockPos.ORIGIN;
	//Will be initialized at TileStructurePiece.initStructure
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
	
	public StructurePiece initStructure(Random rand, @Nullable StructureBase base)
	{
		if(this.structureName!=null)
		{
			if(!this.initialized || this.piece==null)
			{
				Mirror m = base!=null?base.getMirror():this.mirror;
				Rotation r=base!=null?this.rot.add(base.getRot()):this.rot;
				BlockPos offSet = Schematic.transformPos(new Position(0,0,0), m, r, this.offSet);
				this.piece=new StructurePiece(this.structureName[rand.nextInt(this.structureName.length)], 
						m, r, base!=null?base.replaceGround():this.replaceGround, this.pos.add(offSet), base, rand);
				this.initialized=true;
			}
		}
		return this.piece;
	}
	
	public void reset()
	{
		this.initialized=false;
		this.piece=null;
	}
	
	//Manually run the block
	public void runBlock()
	{
		if(this.initStructure(new Random(), null)!=null)
		{
			this.piece.generate(world);
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
		int[] arr = compound.getIntArray("Offset");
		this.offSet=new BlockPos(arr[0],arr[1],arr[2]);
		this.initialized=compound.getBoolean("Initialized");
		if(compound.hasKey("Piece"))
			this.piece=new StructurePiece(compound.getCompoundTag("Piece"));
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
		compound.setIntArray("Offset", new int[] {this.offSet.getX(),this.offSet.getY(),this.offSet.getZ()});
		compound.setBoolean("Initialized", this.initialized);
		if(this.piece!=null)
			compound.setTag("Piece", this.piece.writeToNBT(new NBTTagCompound()));
		return compound;	
	}
}
