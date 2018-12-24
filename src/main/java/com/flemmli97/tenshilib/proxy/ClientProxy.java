package com.flemmli97.tenshilib.proxy;

import com.flemmli97.tenshilib.common.events.handler.ClientEvents;
import com.flemmli97.tenshilib.common.world.structure.StructureBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientProxy extends CommonProxy {
	
	public StructureBase currentStructure;

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
    	MinecraftForge.EVENT_BUS.register(new ClientEvents());
        super.init(e);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
    
    @Override
    public IThreadListener getListener(MessageContext ctx) {
        return ctx.side.isClient() ? Minecraft.getMinecraft() : super.getListener(ctx);
    }
    
	@Override
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
     return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx));
    }
    
    @Override
    public String translate(String s)
    {
    	return I18n.format(s);
    }
    
    @Override
    public void setStructureToRender(StructureBase base)
    {
    	this.currentStructure=base;
    }
    
}