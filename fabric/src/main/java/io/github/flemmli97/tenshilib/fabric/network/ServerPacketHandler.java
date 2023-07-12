package io.github.flemmli97.tenshilib.fabric.network;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.network.PacketRegistrar;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerPacketHandler {

    public static void register() {
        PacketRegistrar.registerServerPackets(new PacketRegistrar.ServerPacketRegister() {
            @Override
            public <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> handler) {
                ServerPlayNetworking.registerGlobalReceiver(id, handlerServer(decoder, handler));
            }
        }, 0);
    }

    public static <T extends Entity & IAnimated> void updateAnimationPkt(T entity) {
        if (entity.level().isClientSide)
            return;
        S2CEntityAnimation pkt = S2CEntityAnimation.create(entity);
        FriendlyByteBuf buf = PacketByteBufs.create();
        pkt.write(buf);
        PlayerLookup.tracking(entity)
                .forEach(player -> ServerPlayNetworking.send(player, pkt.getID(), buf));
    }

    private static <T> ServerPlayNetworking.PlayChannelHandler handlerServer(Function<FriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handler) {
        return (server, player, handler1, buf, responseSender) -> {
            T pkt = decoder.apply(buf);
            server.execute(() -> handler.accept(pkt, player));
        };
    }
}
