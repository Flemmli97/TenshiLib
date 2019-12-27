package com.flemmli97.tenshilib.common.network;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketStructurePiece implements IMessage {

    public NBTTagCompound compound;

    public PacketStructurePiece() {
    }

    /**
     * @param resync wether it should just sent an update to the client again without changing the tile serverside
     */
    public PacketStructurePiece(NBTTagCompound compound, boolean resync) {
        compound.setBoolean("Resync", resync);
        this.compound = compound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.compound);
    }

    public static class Handler implements IMessageHandler<PacketStructurePiece, IMessage> {

        @Override
        public IMessage onMessage(PacketStructurePiece msg, MessageContext ctx) {
            World world = TenshiLib.proxy.getPlayerEntity(ctx).world;
            NBTTagCompound compound = msg.compound;
            BlockPos pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileStructurePiece){
                if(!compound.getBoolean("Resync"))
                    tile.readFromNBT(compound);
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 2);
            }
            return null;
        }
    }
}