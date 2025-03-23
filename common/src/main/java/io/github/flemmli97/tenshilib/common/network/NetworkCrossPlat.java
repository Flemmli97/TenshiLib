package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.platform.InitUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface NetworkCrossPlat {

    NetworkCrossPlat INSTANCE = InitUtil.getPlatformInstance(NetworkCrossPlat.class,
            "io.github.flemmli97.tenshilib.fabric.platform.NetworkPlatImpl",
            "io.github.flemmli97.tenshilib.forge.platform.NetworkPlatImpl");

    void sendToClient(Packet message, ServerPlayer player);

    void sendToServer(Packet message);

    void sendToTracking(Packet message, Entity entity);

}
