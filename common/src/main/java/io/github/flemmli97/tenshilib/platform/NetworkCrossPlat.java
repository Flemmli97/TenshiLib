package io.github.flemmli97.tenshilib.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface NetworkCrossPlat {

    NetworkCrossPlat INSTANCE = InitUtil.getPlatformInstance(NetworkCrossPlat.class,
            "io.github.flemmli97.tenshilib.fabric.platform.NetworkPlatImpl",
            "io.github.flemmli97.tenshilib.forge.platform.NetworkPlatImpl");

    void sendToClient(CustomPacketPayload message, ServerPlayer player);

    void sendToServer(CustomPacketPayload message);

    void sendToTracking(CustomPacketPayload message, Entity entity);

}
