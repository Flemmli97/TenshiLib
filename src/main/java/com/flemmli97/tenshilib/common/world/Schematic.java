package com.flemmli97.tenshilib.common.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.api.block.ITileEntityInitialPlaced;
import com.flemmli97.tenshilib.common.blocks.BlockIgnore;
import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;

import net.minecraft.block.BlockStructure;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.Template;

public class Schematic {
	
	public final int x, y, z;
	private List<TileEntity> tiles;
	private Map<Position, IBlockState> posBlockMapping = new HashMap<Position, IBlockState>();
	private Map<Position, NBTTagCompound> posTileMapping = new HashMap<Position, NBTTagCompound>();
	public Schematic(int width, int height, int length)
	{
		this.x=width;
		this.y=height;
		this.z=length;
	}
	
	public static Schematic fromTemplate(Template t)
	{
		return StructureLoader.loadFromNBT(t.writeToNBT(new NBTTagCompound()));
	}
	
	public void addBlockStateToPos(Position position, IBlockState state)
	{
		this.posBlockMapping.put(position, state);
	}
	
	public void addTileToPos(Position position, NBTTagCompound nbt)
	{
		this.posTileMapping.put(position, nbt);
	}
	
	public List<TileEntity> getTileEntities() 
	{
		return this.tiles;
	}
	
	public IBlockState getBlockAt(BlockPos pos)
	{
		Position pos1 = new Position(pos.getX(), pos.getY(), pos.getZ());
		IBlockState state = this.posBlockMapping.get(pos1);
		return state;
	}
	
	/**
	 * Generates this schematic. Ignores structureblocks. Goes from x to z and then up. 
	 * @param world	World
	 * @param pos The position of the structure. Always at the north-west-deepest (-x,-z) corner of the structure
	 * @param rot Rotation
	 * @param mirror Mirror
	 * @param replaceGroundBelow if it should replace potential air spaces below the structure
	 * @param limitation 
	 */
	public void generate(World world, BlockPos pos, Rotation rot, Mirror mirror, boolean replaceGroundBelow, 
			@Nullable StructureBoundingBox chunkLimitation, @Nullable StructureBase base) {
		for(int y = 0; y < this.y; y++)
			for(int z = 0; z < this.z; z++)
				for(int x = 0; x < this.x; x++)					
				{
					Position schemPos = new Position(x,y,z);
					BlockPos place = transformPos(schemPos, mirror, rot, new BlockPos(this.x, this.y
							, this.z)).add(pos);
					
					if(chunkLimitation!=null && !chunkLimitation.isVecInside(place))
					{
						continue;
					}
					IBlockState state = this.posBlockMapping.get(schemPos);
					//Also checks for structure pieces since they get removed later.
					if(state!=null && !(state.getBlock() instanceof BlockStructure||state.getBlock() instanceof BlockIgnore))
					{
						world.setBlockState(place, state.withMirror(mirror).withRotation(rot), 18);
						if(replaceGroundBelow && y == 0)
						{
							replaceAirAndLiquidDownwards(world, world.getBiome(place).fillerBlock, place.down());
						}
					}
					if(this.posTileMapping.containsKey(schemPos))
					{
						TileEntity tile = world.getTileEntity(place);
						if(tile!=null)
						{
							NBTTagCompound tileNBT = this.posTileMapping.get(schemPos);
							tileNBT.setInteger("x", place.getX());
                            tileNBT.setInteger("y", place.getY());
                            tileNBT.setInteger("z", place.getZ());
							tile.readFromNBT(tileNBT);
							tile.mirror(mirror);
							tile.rotate(rot);
							tile.markDirty();	
							if(tile instanceof ITileEntityInitialPlaced)
								((ITileEntityInitialPlaced)tile).onPlaced(world, place, rot, mirror);
							if(tile instanceof TileStructurePiece)
							{
								TileStructurePiece piece = (TileStructurePiece) tile;
								piece.mirror(mirror);
								piece.setReplaceGround(replaceGroundBelow);
								piece.rotate(rot);
								if(base!=null)
									base.add(piece);
							}
						}
					}
				}
	}
	
	public void generate(World world, BlockPos pos, Rotation rot, Mirror mirror) {
		this.generate(world, pos, rot, mirror, false, null, null);
	}
		
	private static void replaceAirAndLiquidDownwards(World worldIn, IBlockState blockstateIn, BlockPos pos)
    {
        while ((isReplacable(worldIn.getBlockState(pos).getMaterial())||worldIn.isAirBlock(pos)) && pos.getY() > 1)
        {
            worldIn.setBlockState(pos, blockstateIn, 2);
            pos=pos.down();
        }
    }
	
	private static boolean isReplacable(Material mat)
	{
		if(mat==Material.CACTUS||mat==Material.CIRCUITS||mat==Material.CAKE||mat==Material.FIRE||mat==Material.GOURD
				||mat==Material.LAVA||mat==Material.LEAVES|mat==Material.PLANTS||mat==Material.VINE||mat==Material.VINE||mat==Material.WEB||mat==Material.WATER)
			return true;
		return false;
	}

    public static BlockPos transformPos(Position pos, Mirror mirrorIn, Rotation rotationIn, BlockPos oppositeCornerFlat)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        boolean flag = true;

        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                k = oppositeCornerFlat.getZ()-k-1;
                break;
            case FRONT_BACK:
                i = oppositeCornerFlat.getX()-i-1;
                break;
            default:
                flag = false;
        }

        switch (rotationIn)
        {
            case COUNTERCLOCKWISE_90:
                return new BlockPos(k, j, oppositeCornerFlat.getX()-i-1);
            case CLOCKWISE_90:
                return new BlockPos(oppositeCornerFlat.getZ()-k-1, j, i);
            case CLOCKWISE_180:
                return new BlockPos(oppositeCornerFlat.getX()-i-1, j, oppositeCornerFlat.getZ()-k-1);
            default:
                return flag ? new BlockPos(i, j, k) : new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        }
    }
	
	@Override
	public String toString()
	{
		return "Schematic:{[Width:" + this.x + "],[Height:" + this.y + "],[Length:" + this.z+ "],[Tiles:" + this.tiles + "]}";
	}
}
