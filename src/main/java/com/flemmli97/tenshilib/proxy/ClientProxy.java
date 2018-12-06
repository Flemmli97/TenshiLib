package com.flemmli97.tenshilib.proxy;

import com.flemmli97.tenshilib.common.world.StructureBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IThreadListener;
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