package io.github.flemmli97.tenshilib.fabric.platform.patreon;

import io.github.flemmli97.tenshilib.common.network.Packet;
import io.github.flemmli97.tenshilib.fabric.mixin.LivingEntityRendererAccessor;
import io.github.flemmli97.tenshilib.patreon.client.PatreonClientPlatform;
import io.github.flemmli97.tenshilib.patreon.client.PatreonLayer;
import io.github.flemmli97.tenshilib.patreon.client.PatreonModelProvider;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class ClientPatreonImpl implements PatreonClientPlatform {
    @Override
    public void setup() {
        PatreonModelProvider.registerModelLayers(((modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get)));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> PatreonClientPlatform.addPatreonButton(screen));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addPatreonLayer(Map<String, EntityRenderer<? extends Player>> playerRenderers) {
        LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>> r = (LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>>) playerRenderers.get("default");
        ((LivingEntityRendererAccessor) r).add(new PatreonLayer(r));
        r = (LivingEntityRenderer<? extends Player, ? extends EntityModel<? extends Player>>) playerRenderers.get("slim");
        ((LivingEntityRendererAccessor) r).add(new PatreonLayer(r));
    }

    @Override
    public void sendToServer(Packet pkt) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        pkt.write(buf);
        ClientPlayNetworking.send(pkt.getID(), buf);
    }
}
