package io.github.flemmli97.tenshilib.patreon.pkts;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class C2SRequestUpdateClientPkt implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SRequestUpdateClientPkt> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "c2s_update_effect_pkt"));
    public static final C2SRequestUpdateClientPkt INSTANCE = new C2SRequestUpdateClientPkt();
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRequestUpdateClientPkt> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private C2SRequestUpdateClientPkt() {
    }

    public static void handlePacket(C2SRequestUpdateClientPkt pkt, ServerPlayer player) {
        PatreonPlatform.INSTANCE.sendToClient(player, player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

