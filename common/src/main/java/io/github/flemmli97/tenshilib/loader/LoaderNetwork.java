package io.github.flemmli97.tenshilib.loader;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.Collections;

/**
 * Loader agnostic network interface
 */
public interface LoaderNetwork {

    LoaderNetwork INSTANCE = LoaderInitializer.getImplInstance(LoaderNetwork.class,
            "io.github.flemmli97.tenshilib.fabric.loader.LoaderNetworkImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.LoaderNetworkImpl");

    void sendToServer(CustomPacketPayload message);

    void sendToPlayer(CustomPacketPayload message, ServerPlayer player);

    void sendToTracking(CustomPacketPayload message, Entity entity);

    void sendToTracking(CustomPacketPayload message, ServerLevel level, ChunkPos pos);

    void sendToAround(CustomPacketPayload message, ServerLevel level, double x, double y, double z, double radius);

    void sendToAllIn(CustomPacketPayload message, ServerLevel level);

    void sendToAll(CustomPacketPayload message, Collection<ServerPlayer> players);

    default void sendToChecked(CustomPacketPayload message, ServerPlayer players) {
        this.sendToChecked(message, Collections.singleton(players));
    }

    void sendToChecked(CustomPacketPayload message, Collection<ServerPlayer> players);

}
