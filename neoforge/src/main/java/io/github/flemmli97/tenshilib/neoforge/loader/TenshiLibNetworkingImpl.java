package io.github.flemmli97.tenshilib.neoforge.loader;

import io.github.flemmli97.tenshilib.loader.TenshiLibNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public class TenshiLibNetworkingImpl implements TenshiLibNetworking {

    @Override
    public void sendToClient(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    @Override
    public void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    @Override
    public void sendToTracking(CustomPacketPayload message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }
}
