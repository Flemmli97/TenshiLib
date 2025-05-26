package io.github.flemmli97.tenshilib.neoforge;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.neoforge.client.events.ClientEvents;
import io.github.flemmli97.tenshilib.neoforge.events.CommonEvents;
import io.github.flemmli97.tenshilib.neoforge.network.PacketHandler;
import io.github.flemmli97.tenshilib.neoforge.platform.patreon.ClientPatreonImpl;
import io.github.flemmli97.tenshilib.neoforge.platform.patreon.PatreonImpl;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = TenshiLib.MODID)
public class TenshiLibNeoForge {

    public TenshiLibNeoForge(IEventBus modBus) {
        IEventBus eventBus = NeoForge.EVENT_BUS;
        modBus.addListener(TenshiLibNeoForge::preInit);
        modBus.addListener(PacketHandler::register);
        eventBus.addListener(CommonEvents::disableOffhand);
        eventBus.addListener(CommonEvents::disableOffhandBlock);
        eventBus.addListener(CommonEvents::onTracking);
        PatreonImpl.initPatreonData(modBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(ClientEvents::reloadListener);
            modBus.addListener(ClientEvents::itemColors);
            modBus.addListener(ClientEvents::registerShader);
            ClientPatreonImpl.setup(modBus);
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
}
