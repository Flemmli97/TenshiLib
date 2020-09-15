package com.flemmli97.tenshilib;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = TenshiLib.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TenshiLib {

    public static final String MODID = "tenshilib";
    public static final Logger logger = LogManager.getLogger("TenshiLib");

    public static boolean isFateLoaded;
    public static boolean isRunecraftoryLoaded;

    @SubscribeEvent
    public static void preInit(FMLCommonSetupEvent e) {

    }
}
