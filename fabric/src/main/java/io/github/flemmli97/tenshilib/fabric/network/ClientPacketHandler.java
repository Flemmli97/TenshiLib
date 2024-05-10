package io.github.flemmli97.tenshilib.fabric.network;

import io.github.flemmli97.tenshilib.common.network.C2SPacketHit;
import io.github.flemmli97.tenshilib.common.network.PacketRegistrar;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public class ClientPacketHandler {

    public static void register() {
        PacketRegistrar.registerClientPackets(new PacketRegistrar.ClientPacketRegister() {
            @Override
            public <P extends CustomPacketPayload> void register(CustomPacketPayload.Type<P> type, StreamCodec<RegistryFriendlyByteBuf, P> codec, BiConsumer<P, Player> handler) {
                PayloadTypeRegistry.playS2C().register(type, codec);
                ClientPlayNetworking.registerGlobalReceiver(type, (pkt, ctx) -> handler.accept(pkt, ctx.player()));
            }
        });
    }

    public static void sendWeaponHitPkt(boolean isAOE) {
        C2SPacketHit pkt = new C2SPacketHit(isAOE ? C2SPacketHit.HitType.AOE : C2SPacketHit.HitType.EXT);
        ClientPlayNetworking.send(pkt);
    }
}
