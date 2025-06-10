package io.github.flemmli97.tenshilib.neoforge.loader;

import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public class LoaderNetworkImpl implements LoaderNetwork {

    @Override
    public void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    @Override
    public void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    @Override
    public void sendToTracking(CustomPacketPayload message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }

    @Override
    public void sendToTracking(CustomPacketPayload message, ServerLevel level, ChunkPos pos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, pos, message);
    }

    @Override
    public void sendToAround(CustomPacketPayload message, ServerLevel level, double x, double y, double z, double radius) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, message);
    }

    @Override
    public void sendToAllIn(CustomPacketPayload message, ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, message);
    }

    @Override
    public void sendToAll(CustomPacketPayload message, MinecraftServer server) {
        PacketDistributor.sendToAllPlayers(message);
    }
}
