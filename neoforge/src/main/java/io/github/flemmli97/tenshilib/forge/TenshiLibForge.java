package io.github.flemmli97.tenshilib.forge;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.forge.client.events.ClientEvents;
import io.github.flemmli97.tenshilib.forge.events.CommonEvents;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import io.github.flemmli97.tenshilib.forge.platform.patreon.ClientPatreonImpl;
import io.github.flemmli97.tenshilib.forge.platform.patreon.PatreonImpl;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = TenshiLib.MODID)
public class TenshiLibForge {

    public TenshiLibForge(IEventBus modBus) {
        IEventBus forgeBus = NeoForge.EVENT_BUS;
        modBus.addListener(TenshiLibForge::preInit);
        modBus.addListener(PacketHandler::register);
        forgeBus.addListener(CommonEvents::disableOffhand);
        forgeBus.addListener(CommonEvents::disableOffhandBlock);
        forgeBus.addListener(CommonEvents::onTracking);
        PatreonImpl.initPatreonData(modBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(ClientEvents::reloadListener);
            modBus.addListener(ClientEvents::itemColors);
            modBus.addListener(ClientEvents::registerShader);
            ClientPatreonImpl.setup(modBus);
            forgeBus.addListener(ClientEvents::clickSpecial);
            forgeBus.addListener(ClientEvents::onEntityRender);
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
