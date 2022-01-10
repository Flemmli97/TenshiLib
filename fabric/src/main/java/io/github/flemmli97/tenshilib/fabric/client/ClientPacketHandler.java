package io.github.flemmli97.tenshilib.fabric.client;

import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.fabric.network.PacketID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class ClientPacketHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PacketID.animationPacket, ClientPacketHandler::animMessage);
    }

    public static void sendWeaponHitPkt(boolean isAOE) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isAOE);
        ClientPlayNetworking.send(PacketID.hitPacket, buf);
    }

    private static void animMessage(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ClientHandlers.updateAnim(buf.readInt(), buf.readInt());
    }
}
