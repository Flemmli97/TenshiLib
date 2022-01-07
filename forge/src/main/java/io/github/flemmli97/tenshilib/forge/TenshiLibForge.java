package io.github.flemmli97.tenshilib.forge;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.forge.client.events.ClientEvents;
import io.github.flemmli97.tenshilib.forge.events.CommonEvents;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(value = TenshiLib.MODID)
public class TenshiLibForge {

    public TenshiLibForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        modBus.addListener(TenshiLibForge::preInit);
        forgeBus.addListener(CommonEvents::leftClickBlock);
        forgeBus.addListener(CommonEvents::disableOffhand);
        forgeBus.addListener(CommonEvents::disableOffhandBlock);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(ClientEvents::reloadListener);
            modBus.addListener(ClientEvents::itemColors);
            forgeBus.addListener(ClientEvents::clickSpecial);
        }
    }

    public static void preInit(FMLCommonSetupEvent e) {
        PacketHandler.register();
        e.enqueueWork(() -> {
            for (SpawnEgg egg : SpawnEgg.getEggs())
                DispenserBlock.registerBehavior(egg, egg.dispenser());
        });
    }
}
