package io.github.flemmli97.tenshilib.fabric.loader;

import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class LoaderNetworkImpl implements LoaderNetwork {

    @Override
    public void sendToServer(CustomPacketPayload message) {
        ClientPlayNetworking.send(message);
    }

    @Override
    public void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }

    @Override
    public void sendToTracking(CustomPacketPayload message, Entity entity) {
        PlayerLookup.tracking(entity)
                .forEach(player -> ServerPlayNetworking.send(player, message));
    }

    @Override
    public void sendToTracking(CustomPacketPayload message, ServerLevel level, ChunkPos pos) {
        PlayerLookup.tracking(level, pos)
                .forEach(player -> ServerPlayNetworking.send(player, message));
    }

    @Override
    public void sendToAround(CustomPacketPayload message, ServerLevel level, double x, double y, double z, double radius) {
        PlayerLookup.around(level, new Vec3(x, y, z), radius)
                .forEach(player -> ServerPlayNetworking.send(player, message));
    }

    @Override
    public void sendToAllIn(CustomPacketPayload message, ServerLevel level) {
        PlayerLookup.world(level)
                .forEach(player -> ServerPlayNetworking.send(player, message));
    }

    @Override
    public void sendToAll(CustomPacketPayload message, MinecraftServer server) {
        PlayerLookup.all(server)
                .forEach(player -> ServerPlayNetworking.send(player, message));
    }
}
