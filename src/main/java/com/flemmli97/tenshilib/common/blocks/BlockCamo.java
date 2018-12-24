/*package com.flemmli97.tenshilib.common.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.common.blocks.tile.TileCamo;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCamo extends Block implements ITileEntityProvider{
		
	private static final PropertyInteger prop = PropertyInteger.create("state", 0, 3);
	
	public BlockCamo(ResourceLocation res, Material mat) {
		super(mat);
		this.setRegistryName(res);
		this.setUnlocalizedName(this.getRegistryName().toString());
		this.setDefaultState(this.getDefaultState().withProperty(prop, 3));
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
		TileCamo tile = (TileCamo) worldIn.getTileEntity(pos);
    	return (tile!=null && tile.state!=null && worldIn instanceof ChunkCache)?tile.state:state;
    }
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState)
    {
		TileCamo tile = (TileCamo) worldIn.getTileEntity(pos);
        IBlockState other =  (tile!=null && tile.state!=null)?tile.state:state;
        if(state!=other)
        	other.addCollisionBoxToList(worldIn, pos, entityBox, collidingBoxes, entityIn, false);
        else
        	addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileCamo tile = (TileCamo) source.getTileEntity(pos);
        return (tile!=null && tile.state!=null)?tile.state.getBoundingBox(source, pos):Block.FULL_BLOCK_AABB;
	}
	
    @Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    	if(!worldIn.isRemote)
    	{
    		ItemStack stack = playerIn.getHeldItem(hand);
    		Block block = Block.getBlockFromItem(stack.getItem());
    		if(block!=null && block!=this)
    		{
    			IBlockState blockState = block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, stack.getMetadata(), playerIn, hand);
    			TileCamo tile = (TileCamo) worldIn.getTileEntity(pos);
    			tile.state=blockState;
    			int opaque = blockState.isOpaqueCube()?1:0;
    			int full = blockState.isFullBlock()?2:0;
    			worldIn.setBlockState(pos, state.withProperty(prop, full+opaque), 2);
    			worldIn.notifyBlockUpdate(pos, state, state.withProperty(prop, full+opaque), 3);
    			return true;
    		}
    	}
		return false;
	}
    
    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
    	TileCamo tile = (TileCamo) worldIn.getTileEntity(pos);
        if(tile!=null && tile.state!=null)
        	tile.state.getBlock().randomDisplayTick(tile.state, worldIn, pos, rand);
    }
    
    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
    	TileCamo tile = (TileCamo) world.getTileEntity(pos);
        return (tile!=null && tile.state!=null)?tile.state.doesSideBlockRendering(world, pos, face):true;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return (state.getValue(prop) == 1)||(state.getValue(prop) == 3);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return (state.getValue(prop)==2)||(state.getValue(prop) == 3);
    }
    

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(prop, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(prop);
	}
	
    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {prop});
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileCamo();
	}
}*/
