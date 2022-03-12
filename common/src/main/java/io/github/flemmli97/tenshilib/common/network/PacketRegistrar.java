package io.github.flemmli97.tenshilib.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class PacketRegistrar {


    public static int registerServerPackets(ServerPacketRegister register, int id) {
        register.registerMessage(id++, C2SPacketHit.ID, C2SPacketHit.class, C2SPacketHit::write, C2SPacketHit::fromBytes, C2SPacketHit::handlePacket);
        return id;
    }

    public static int registerClientPackets(ClientPacketRegister register, int id) {
        register.registerMessage(id++, S2CEntityAnimation.ID, S2CEntityAnimation.class, S2CEntityAnimation::write, S2CEntityAnimation::fromBytes, S2CEntityAnimation::handlePacket);
        return id;
    }

    public interface ServerPacketRegister {
        <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> handler);
    }

    public interface ClientPacketRegister {
        <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, Consumer<P> handler);
    }
}
