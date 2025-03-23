package io.github.flemmli97.tenshilib.fabric.platform;

import io.github.flemmli97.tenshilib.common.network.NetworkCrossPlat;
import io.github.flemmli97.tenshilib.common.network.Packet;
import io.github.flemmli97.tenshilib.fabric.network.ClientPacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class NetworkPlatImpl implements NetworkCrossPlat {

    @Override
    public void sendToClient(Packet message, ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        ServerPlayNetworking.send(player, message.getID(), buf);
    }

    @Override
    public void sendToServer(Packet message) {
        ClientPacketHandler.sendToServer(message);
    }

    @Override
    public void sendToTracking(Packet message, Entity entity) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        message.write(buf);
        PlayerLookup.tracking(entity)
                .forEach(player -> ServerPlayNetworking.send(player, message.getID(), buf));
    }
}
