package com.flemmli97.tenshilib.proxy;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.asm.ASMException;
import com.flemmli97.tenshilib.asm.ASMLoader;
import com.flemmli97.tenshilib.client.gui.GuiHandler;
import com.flemmli97.tenshilib.common.config.ConfigHandler;
import com.flemmli97.tenshilib.common.events.handler.CommonEvents;
import com.flemmli97.tenshilib.common.item.ItemUtil;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.world.structure.StructureBase;
import com.flemmli97.tenshilib.common.world.structure.StructureGenerator;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

    public static boolean isFateLoaded;
    public static boolean isRunecraftoryLoaded;

    public void preInit(FMLPreInitializationEvent e) {
        if(!ASMLoader.asmLoaded)
            throw new ASMException.ASMLoadException();
        ConfigHandler.load();
        PacketHandler.registerPackets();
        isFateLoaded = Loader.isModLoaded("fatemod");
        isRunecraftoryLoaded = Loader.isModLoaded("runecraftory");
    }

    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        NetworkRegistry.INSTANCE.registerGuiHandler(TenshiLib.instance, new GuiHandler());
        ItemUtil.initItemLists();
        GameRegistry.registerWorldGenerator(new StructureGenerator(), 1);
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    public IThreadListener getListener(MessageContext ctx) {
        return (WorldServer) ctx.getServerHandler().player.world;
    }

    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return ctx.getServerHandler().player;
    }

    /**
     * Client needs translation
     */
    public String translate(String string) {
        return string;
    }

    public void setStructureToRender(StructureBase structure) {

    }

    public void spawnParticle(ResourceLocation res, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed,
            double zSpeed, Object... parameters) {

    }

    public void spawnParticle(Particle particle) {

    }
}
