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
import java.util.function.Consumer;

public class PacketRegistrar {

    public static void registerServerPackets(ServerPacketRegister register) {
        register.register(C2SAttackPacket.TYPE, C2SAttackPacket.STREAM_CODEC, C2SAttackPacket::handle);
        register.register(C2SEffectUpdatePkt.TYPE, C2SEffectUpdatePkt.STREAM_CODEC, C2SEffectUpdatePkt::handle);
        register.register(C2SRequestUpdateClientPkt.TYPE, C2SRequestUpdateClientPkt.STREAM_CODEC, C2SRequestUpdateClientPkt::handle);
        register.register(C2SAnimationDebuggerUpdate.TYPE, C2SAnimationDebuggerUpdate.STREAM_CODEC, C2SAnimationDebuggerUpdate::handle);
    }

    public static void registerClientPackets(ClientPacketRegister register) {
        register.register(S2CEntityAnimation.TYPE, S2CEntityAnimation.STREAM_CODEC, S2CEntityAnimation::handle);
        register.register(S2CEffectUpdatePkt.TYPE, S2CEffectUpdatePkt.STREAM_CODEC, S2CEffectUpdatePkt::handleClient);
        register.register(S2CAnimationScreen.TYPE, S2CAnimationScreen.STREAM_CODEC, S2CAnimationScreen::handle);
        register.register(S2CAnimationDataPacket.TYPE, S2CAnimationDataPacket.STREAM_CODEC, S2CAnimationDataPacket::handle);
    }

    public interface ServerPacketRegister {

        <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, BiConsumer<P, ServerPlayer> handler);
    }

    public interface ClientPacketRegister {

        default <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, Consumer<P> handler) {
            this.register(type, codec, (pkt, p) -> handler.accept(pkt));
        }

        <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, BiConsumer<P, Player> handler);
    }
}
