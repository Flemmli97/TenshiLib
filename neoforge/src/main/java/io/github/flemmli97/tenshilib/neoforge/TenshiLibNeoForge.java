package io.github.flemmli97.tenshilib.neoforge;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.neoforge.client.events.ClientEvents;
import io.github.flemmli97.tenshilib.neoforge.client.events.PatreonClientSetup;
import io.github.flemmli97.tenshilib.neoforge.events.CommonEvents;
import io.github.flemmli97.tenshilib.neoforge.loader.patreon.TenshiLibPatreonImpl;
import io.github.flemmli97.tenshilib.neoforge.network.PacketHandler;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(value = TenshiLib.MODID)
public class TenshiLibNeoForge {

    public TenshiLibNeoForge(IEventBus modBus) {
        IEventBus eventBus = NeoForge.EVENT_BUS;
        modBus.addListener(TenshiLibNeoForge::preInit);
        modBus.addListener(PacketHandler::register);
        eventBus.addListener(CommonEvents::leftClickBlock);
        eventBus.addListener(CommonEvents::disableOffhand);
        eventBus.addListener(CommonEvents::disableOffhandBlock);
        eventBus.addListener(CommonEvents::onTracking);
        TenshiLibPatreonImpl.initPatreonData(modBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(ClientEvents::reloadListener);
            modBus.addListener(ClientEvents::itemColors);
            modBus.addListener(ClientEvents::registerShader);
            PatreonClientSetup.setup(modBus);
            eventBus.addListener(ClientEvents::clickSpecial);
            eventBus.addListener(ClientEvents::onEntityRender);
        }
    }

    public static void preInit(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            for (SpawnEgg egg : SpawnEgg.getEggs())
                DispenserBlock.registerBehavior(egg, egg.dispenser());
            SpawnEgg.resolveEggs();
        });
    }

    public static void reloadListener(AddReloadListenerEvent event) {
        event.addListener(AnimationDataManager.getInstance());
    }
}
