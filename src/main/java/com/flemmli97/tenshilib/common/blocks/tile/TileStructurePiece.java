package com.flemmli97.tenshilib.common.blocks.tile;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.common.world.Position;
import com.flemmli97.tenshilib.common.world.structure.GenerationType;
import com.flemmli97.tenshilib.common.world.structure.Schematic;
import com.flemmli97.tenshilib.common.world.structure.StructureBase;
import com.flemmli97.tenshilib.common.world.structure.StructureLoader;
import com.flemmli97.tenshilib.common.world.structure.StructurePiece;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class TileStructurePiece extends TileEntity{

	private Map<Float, List<ResourceLocation>> structureNames = new TreeMap<Float,List<ResourceLocation>>();
	
	private StructurePiece piece;
	private Mirror mirror=Mirror.NONE;
	private Rotation rot = Rotation.NONE;
	private GenerationType genType = GenerationType.FLOATING;
	private BlockPos offSet = BlockPos.ORIGIN;
	//Will be initialized at TileStructurePiece.initStructure
	private boolean initialized;
	
	public TileStructurePiece() {}
	
	public void addStructureName(float chance, ResourceLocation res)
	{
		this.structureNames.merge(chance, res!=null?Lists.newArrayList(res):Lists.newArrayList(), (old, val)->
		{val.forEach(value->
			{
				if(!old.contains(value))
					old.add(value);
			});return old;
		});
	}
	
	public void removeStructureName(ResourceLocation res, float chance)
	{
		this.structureNames.entrySet().removeIf(new Predicate<Entry<Float,List<ResourceLocation>>>() {
			@Override
			public boolean test(Entry<Float, List<ResourceLocation>> entry) {
				if(entry.getKey().equals(chance))
					entry.getValue().remove(res);
				return entry.getValue().isEmpty();
			}});
	}
	
	public Set<Entry<Float, List<ResourceLocation>>> entries()
	{
		return ImmutableSet.copyOf(this.structureNames.entrySet());
	}
	
	public void clearStructureNames()
	{
		this.structureNames.clear();
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
	
	public void setGenerationType(GenerationType genType)
	{
		this.genType=genType;
	}
	
	public GenerationType generationType()
	{
		return this.genType;
	}
	
	public void setOffSet(BlockPos pos)
	{
		this.offSet=pos;
	}
	
	public BlockPos offSet()
	{
		return this.offSet;
	}
	
	public StructurePiece initStructure(Random rand, @Nullable StructureBase base)
	{
		if(!this.initialized)
		{
			ResourceLocation res=null;
			for(Entry<Float, List<ResourceLocation>> e : this.structureNames.entrySet())
			{
				if(rand.nextFloat()<e.getKey())
					res=e.getValue().get(rand.nextInt(e.getValue().size()));
			}
			if(res!=null)
			{
				Schematic schematic = StructureLoader.getSchematic(res);
				if(schematic!=null)
				{
					Mirror m = base!=null?base.getMirror():this.mirror;
					Rotation r=base!=null?this.rot.add(base.getRot()):this.rot;
					BlockPos offSet = Schematic.transformPos(new Position(0,0,0), m, r, this.offSet);
					this.piece=new StructurePiece(res , 
							m, r, base!=null?base.genType():this.genType, this.pos.add(offSet), base, rand);
				}
			}
			this.initialized=true;
		}
		return this.piece;
	}
	
	public void reset()
	{
		this.initialized=false;
		this.piece=null;
	}
	
	public boolean initialized()
	{
		return this.initialized;
	}
	
	//Manually run the block
	public void runBlock()
	{
		if(this.initStructure(new Random(), null)!=null)
			this.piece.generate(world);
		this.world.notifyBlockUpdate(this.getPos(), this.world.getBlockState(this.getPos()), this.world.getBlockState(this.getPos()), 2);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.structureNames.clear();
		NBTTagCompound tag = compound.getCompoundTag("Structures");
		tag.getKeySet().forEach(key->{
			this.addStructureName(tag.getFloat(key), key.equals("EMPTY")?null:new ResourceLocation(key));
		});
		this.mirror = Mirror.valueOf(compound.getString("Mirror"));
		this.rot = Rotation.valueOf(compound.getString("Rotation"));
		this.genType = GenerationType.valueOf(compound.getString("GenerationType"));
		int[] arr = compound.getIntArray("Offset");
		this.offSet=new BlockPos(arr[0],arr[1],arr[2]);
		this.initialized=compound.getBoolean("Initialized");
		if(compound.hasKey("Piece"))
			this.piece=new StructurePiece(compound.getCompoundTag("Piece"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagCompound tag = new NBTTagCompound();
		this.structureNames.entrySet().forEach(entry->{
			entry.getValue().forEach(res->{
				tag.setFloat(res!=null?res.toString():"EMPTY", entry.getKey());
			});
		});
		compound.setTag("Structures", tag);
		compound.setString("Mirror", this.mirror.toString());
		compound.setString("Rotation", this.rot.toString());
		compound.setString("GenerationType", this.genType.toString());
		compound.setIntArray("Offset", new int[] {this.offSet.getX(),this.offSet.getY(),this.offSet.getZ()});
		compound.setBoolean("Initialized", this.initialized);
		if(this.piece!=null)
			compound.setTag("Piece", this.piece.writeToNBT(new NBTTagCompound()));
		return compound;	
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public final SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
	}
}
