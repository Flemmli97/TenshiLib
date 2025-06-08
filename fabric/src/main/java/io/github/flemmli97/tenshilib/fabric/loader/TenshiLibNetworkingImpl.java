package io.github.flemmli97.tenshilib.fabric.loader;

import io.github.flemmli97.tenshilib.loader.TenshiLibNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TenshiLibNetworkingImpl implements TenshiLibNetworking {

    @Override
    public void sendToClient(CustomPacketPayload message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }

    @Override
    public void sendToServer(CustomPacketPayload message) {
        ClientPlayNetworking.send(message);
    }

    @Override
    public void sendToTracking(CustomPacketPayload message, Entity entity) {
        PlayerLookup.tracking(entity)
                .forEach(player -> ServerPlayNetworking.send(player, message));
    }
}
