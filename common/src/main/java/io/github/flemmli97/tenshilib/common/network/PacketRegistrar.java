package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.patreon.pkts.C2SEffectUpdatePkt;
import io.github.flemmli97.tenshilib.patreon.pkts.C2SRequestUpdateClientPkt;
import io.github.flemmli97.tenshilib.patreon.pkts.S2CEffectUpdatePkt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketRegistrar {

    public static int registerServerPackets(ServerPacketRegister register, int id) {
        register.registerMessage(id++, C2SPacketHit.ID, C2SPacketHit.class, C2SPacketHit::write, C2SPacketHit::fromBytes, C2SPacketHit::handlePacket);
        register.registerMessage(id++, C2SEffectUpdatePkt.ID, C2SEffectUpdatePkt.class, C2SEffectUpdatePkt::write, C2SEffectUpdatePkt::fromBytes, C2SEffectUpdatePkt::handlePacketServer);
        register.registerMessage(id++, C2SRequestUpdateClientPkt.ID, C2SRequestUpdateClientPkt.class, C2SRequestUpdateClientPkt::write, C2SRequestUpdateClientPkt::fromBytes, C2SRequestUpdateClientPkt::handlePacketServer);
        return id;
    }

    public static int registerClientPackets(ClientPacketRegister register, int id) {
        register.registerMessage(id++, S2CEntityAnimation.ID, S2CEntityAnimation.class, S2CEntityAnimation::write, S2CEntityAnimation::fromBytes, S2CEntityAnimation.Handler::handlePacket);
        register.registerMessage(id++, S2CEffectUpdatePkt.ID, S2CEffectUpdatePkt.class, S2CEffectUpdatePkt::write, S2CEffectUpdatePkt::fromBytes, S2CEffectUpdatePkt.Handler::handlePktClient);
        return id;
    }

    public interface ServerPacketRegister {
        <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> handler);
    }

    public interface ClientPacketRegister {
        <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, Consumer<P> handler);
    }
}
