package io.github.flemmli97.tenshilib.patreon.pkts;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.network.Packet;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class C2SRequestUpdateClientPkt implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "c2s_update_effect_pkt");

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public static C2SRequestUpdateClientPkt fromBytes(FriendlyByteBuf buf) {
        return new C2SRequestUpdateClientPkt();
    }

    public static void handlePacketServer(C2SRequestUpdateClientPkt pkt, ServerPlayer player) {
        PatreonPlatform.INSTANCE.sendToClient(player, player);
    }
}

