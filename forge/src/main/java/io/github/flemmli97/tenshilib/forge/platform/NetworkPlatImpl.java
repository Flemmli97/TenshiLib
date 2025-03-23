package io.github.flemmli97.tenshilib.forge.platform;

import io.github.flemmli97.tenshilib.common.network.NetworkCrossPlat;
import io.github.flemmli97.tenshilib.common.network.Packet;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class NetworkPlatImpl implements NetworkCrossPlat {

    @Override
    public void sendToClient(Packet message, ServerPlayer player) {
        PacketHandler.sendToClientChecked(message, player);
    }

    @Override
    public void sendToServer(Packet message) {
        PacketHandler.sendToServer(message);
    }

    @Override
    public void sendToTracking(Packet message, Entity entity) {
        PacketHandler.sendToTracking(message, entity);
    }
}
