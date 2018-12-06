package com.flemmli97.tenshilib;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.flemmli97.tenshilib.common.commands.CommandItemData;
import com.flemmli97.tenshilib.common.commands.CommandLocateStructure;
import com.flemmli97.tenshilib.common.commands.CommandStructure;
import com.flemmli97.tenshilib.proxy.CommonProxy;

import net.minecraft.world.GameRules.ValueType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = TenshiLib.MODID, name = TenshiLib.MODNAME, version = TenshiLib.VERSION)
public class TenshiLib {

    public static final String MODID = "tenshilib";
    public static final String MODNAME = "TenshiLib";
    public static final String VERSION = "1.0.0";
    public static final Logger logger = LogManager.getLogger(TenshiLib.MODNAME);
        
    @Instance
    public static TenshiLib instance = new TenshiLib();
        
    @SidedProxy(clientSide="com.flemmli97.tenshilib.proxy.ClientProxy", serverSide="com.flemmli97.tenshilib.proxy.CommonProxy")
    public static CommonProxy proxy;

    //The modpack/minecraft folder
    public static File instanceFolder;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
        instanceFolder=e.getModConfigurationDirectory().getParentFile();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandItemData());
    	event.registerServerCommand(new CommandStructure());
    	event.registerServerCommand(new CommandLocateStructure());
    	if(!event.getServer().getWorld(0).getGameRules().hasRule("showBoundingBox"))
    		event.getServer().getWorld(0).getGameRules().addGameRule("showBoundingBox", "false", ValueType.BOOLEAN_VALUE);
    }
}
    