package com.flemmli97.tenshilib;

import com.flemmli97.tenshilib.client.events.handler.ClientEvents;
import com.flemmli97.tenshilib.common.events.CommonEvents;
import com.flemmli97.tenshilib.common.item.SpawnEgg;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import net.minecraft.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = TenshiLib.MODID)
public class TenshiLib {

    public static final String MODID = "tenshilib";
    public static final Logger logger = LogManager.getLogger("TenshiLib");

    public static boolean isFateLoaded;
    public static boolean isRunecraftoryLoaded;

    public TenshiLib() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        modBus.addListener(this::preInit);
        forgeBus.addListener(CommonEvents::leftClickBlock);
        modBus.addListener(ClientEvents::itemColors);
        forgeBus.addListener(ClientEvents::clickSpecial);
    }

    @SubscribeEvent
    public void preInit(FMLCommonSetupEvent e) {
        PacketHandler.register();
        e.enqueueWork(() -> {
            for (SpawnEgg egg : SpawnEgg.getEggs())
                DispenserBlock.registerDispenseBehavior(egg, egg.dispenser());
        });
    }

}
