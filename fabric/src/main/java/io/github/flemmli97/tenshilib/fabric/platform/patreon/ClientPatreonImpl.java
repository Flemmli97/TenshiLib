package io.github.flemmli97.tenshilib.fabric.platform.patreon;

import io.github.flemmli97.tenshilib.fabric.mixin.LivingEntityRendererAccessor;
import io.github.flemmli97.tenshilib.patreon.client.PatreonClientPlatform;
import io.github.flemmli97.tenshilib.patreon.client.PatreonLayer;
import io.github.flemmli97.tenshilib.patreon.client.PatreonModelProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class ClientPatreonImpl implements PatreonClientPlatform {

    public static void setup() {
        PatreonModelProvider.registerModelLayers(((modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get)));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> PatreonClientPlatform.addPatreonButton(screen));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addPatreonLayer(Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers) {
        LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>> r = (LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>>) playerRenderers.get(PlayerSkin.Model.WIDE);
        ((LivingEntityRendererAccessor) r).add(new PatreonLayer(r));
        r = (LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>>) playerRenderers.get(PlayerSkin.Model.SLIM);
        ((LivingEntityRendererAccessor) r).add(new PatreonLayer(r));
    }

    @Override
    public void sendToServer(CustomPacketPayload pkt) {
        ClientPlayNetworking.send(pkt);
    }
}
