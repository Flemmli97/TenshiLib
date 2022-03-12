package io.github.flemmli97.tenshilib.fabric.network;

import io.github.flemmli97.tenshilib.common.network.C2SPacketHit;
import io.github.flemmli97.tenshilib.common.network.PacketRegistrar;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientPacketHandler {

    public static void register() {
        PacketRegistrar.registerClientPackets(new PacketRegistrar.ClientPacketRegister() {
            @Override
            public <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, Consumer<P> handler) {
                ClientPlayNetworking.registerGlobalReceiver(id, handlerClient(decoder, handler));
            }
        }, 0);
    }

    public static void sendWeaponHitPkt(boolean isAOE) {
        C2SPacketHit pkt = new C2SPacketHit(isAOE ? C2SPacketHit.HitType.AOE : C2SPacketHit.HitType.EXT);
        FriendlyByteBuf buf = PacketByteBufs.create();
        pkt.write(buf);
        ClientPlayNetworking.send(pkt.getID(), buf);
    }

    private static <T> ClientPlayNetworking.PlayChannelHandler handlerClient(Function<FriendlyByteBuf, T> decoder, Consumer<T> handler) {
        return (client, handler1, buf, responseSender) -> {
            T pkt = decoder.apply(buf);
            client.execute(() -> handler.accept(pkt));
        };
    }
}
