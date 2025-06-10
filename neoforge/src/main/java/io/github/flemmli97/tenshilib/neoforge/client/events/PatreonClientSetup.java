package io.github.flemmli97.tenshilib.neoforge.client.events;

import io.github.flemmli97.tenshilib.patreon.client.PatreonLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class PatreonClientSetup {

    public static void setup(IEventBus modBus) {
        modBus.addListener(PatreonClientSetup::addLayerEvent);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerEvent(EntityRenderersEvent.AddLayers event) {
        LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>> r = event.getSkin(PlayerSkin.Model.WIDE);
        if (r != null) {
            r.addLayer(new PatreonLayer(r));
        }
        r = event.getSkin(PlayerSkin.Model.SLIM);
        if (r != null) {
            r.addLayer(new PatreonLayer(r));
        }
    }
}
