/*package com.flemmli97.tenshilib.common.blocks.tile;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileCamo extends TileEntity{

	public TileCamo() {}
	public IBlockState state;
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = new NBTTagCompound();
		if(state != null) {
			compound.setString("camo", Block.REGISTRY.getNameForObject(state.getBlock()).toString());
			compound.setInteger("meta", state.getBlock().getMetaFromState(state));
		}
		return compound;
	}
	
	@Override
	public final SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, -1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
		NBTTagCompound compound = packet.getNbtCompound();
		Block b = Block.getBlockFromName(compound.getString("camo"));
		if (b != null) {
			state = b.getStateFromMeta(compound.getInteger("meta"));
		}
		world.markBlockRangeForRenderUpdate(pos, pos);
	}
}
*/