package io.github.flemmli97.tenshilib.fabric.network;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.network.PacketRegistrar;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public class PacketHandler {

    public static void register() {
        PacketRegistrar.registerServerPackets(new PacketRegistrar.ServerPacketRegister() {
            @Override
            public <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, BiConsumer<P, ServerPlayer> handler) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (pkt, ctx) -> handler.accept(pkt, ctx.player()));
            }
        });
        PacketRegistrar.registerClientPackets(new PacketRegistrar.ClientPacketRegister() {
            @Override
            public <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, BiConsumer<P, Player> handler) {
                PayloadTypeRegistry.playS2C().register(type, codec);
                if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    ClientPlayNetworking.registerGlobalReceiver(type, (pkt, ctx) -> handler.accept(pkt, ctx.player()));
                }
            }
        });
    }

    public static <T extends Entity & IAnimated> void updateAnimationPkt(T entity) {
        if (entity.level().isClientSide)
            return;
        S2CEntityAnimation pkt = S2CEntityAnimation.create(entity);
        PlayerLookup.tracking(entity)
                .forEach(player -> ServerPlayNetworking.send(player, pkt));
    }
}
