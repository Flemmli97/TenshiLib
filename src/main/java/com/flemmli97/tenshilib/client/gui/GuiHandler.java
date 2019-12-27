package com.flemmli97.tenshilib.client.gui;

import com.flemmli97.tenshilib.common.blocks.tile.TileStructurePiece;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == 0){
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if(tile instanceof TileStructurePiece)
                return new GuiStructure(Minecraft.getMinecraft(), (TileStructurePiece) tile);
        }
        return null;
    }
}
