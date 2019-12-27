package com.flemmli97.tenshilib.common.blocks;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStructurePiece extends BlockIgnore implements ITileEntityProvider {

    public BlockStructurePiece() {
        super("structure_piece");
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX,
            float hitY, float hitZ) {
        player.openGui(TenshiLib.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if(world.isBlockPowered(pos)){
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileStructurePiece)
                ((TileStructurePiece) tile).runBlock();
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileStructurePiece();
    }
}
