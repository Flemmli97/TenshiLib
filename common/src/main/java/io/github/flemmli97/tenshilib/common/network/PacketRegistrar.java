package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SRequestUpdateClientPkt;
import io.github.flemmli97.tenshilib.patreon.pkts.S2CEffectUpdatePkt;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public class PacketRegistrar {

    public static void registerServerPackets(ServerPacketRegister register) {
        register.register(C2SPacketHit.TYPE, C2SPacketHit.STREAM_CODEC, C2SPacketHit::handlePacket);
        register.register(C2SEffectUpdatePkt.TYPE, C2SEffectUpdatePkt.STREAM_CODEC, C2SEffectUpdatePkt::handlePacket);
        register.register(C2SRequestUpdateClientPkt.TYPE, C2SRequestUpdateClientPkt.STREAM_CODEC, C2SRequestUpdateClientPkt::handlePacket);
    }

    public static void registerClientPackets(ClientPacketRegister register) {
        register.register(S2CEntityAnimation.TYPE, S2CEntityAnimation.STREAM_CODEC, S2CEntityAnimation.Handler::handlePacket);
        register.register(S2CEffectUpdatePkt.TYPE, S2CEffectUpdatePkt.STREAM_CODEC, S2CEffectUpdatePkt.Handler::handlePacket);
    }

    public interface ServerPacketRegister {
        <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, BiConsumer<P, ServerPlayer> handler);
    }

    public interface ClientPacketRegister {
        <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, BiConsumer<P, Player> handler);
    }
}
