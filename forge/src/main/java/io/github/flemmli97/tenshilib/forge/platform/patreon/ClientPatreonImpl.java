package io.github.flemmli97.tenshilib.forge.platform.patreon;

import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import io.github.flemmli97.tenshilib.patreon.client.PatreonClientPlatform;
import io.github.flemmli97.tenshilib.patreon.client.PatreonLayer;
import io.github.flemmli97.tenshilib.patreon.client.PatreonModelProvider;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

public class ClientPatreonImpl implements PatreonClientPlatform {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setup() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Consumer<EntityRenderersEvent.AddLayers> layer = event -> {
            LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>> r = event.getSkin("default");
            r.addLayer(new PatreonLayer(r));
            r = event.getSkin("slim");
            r.addLayer(new PatreonLayer(r));
        };
        modBus.addListener(layer);
        Consumer<EntityRenderersEvent.RegisterLayerDefinitions> ml = event -> PatreonModelProvider.registerModelLayers(event::registerLayerDefinition);
        modBus.addListener(ml);

        Consumer<ScreenEvent.Init.Post> screen = event -> PatreonClientPlatform.addPatreonButton(event.getScreen());
        MinecraftForge.EVENT_BUS.addListener(screen);
    }

    @Override
    public void sendToServer(C2SEffectUpdatePkt pkt) {
        PacketHandler.sendToServer(pkt);
    }
}
