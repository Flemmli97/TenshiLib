package io.github.flemmli97.tenshilib.loader;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface TenshiLibNetworking {

    TenshiLibNetworking INSTANCE = LoaderInitializer.getImplInstance(TenshiLibNetworking.class,
            "io.github.flemmli97.tenshilib.fabric.loader.NetworkPlatImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.NetworkPlatImpl");

    void sendToClient(CustomPacketPayload message, ServerPlayer player);

    void sendToServer(CustomPacketPayload message);

    void sendToTracking(CustomPacketPayload message, Entity entity);

}
