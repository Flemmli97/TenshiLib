package io.github.flemmli97.tenshilib.forge.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.network.PacketRegistrar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {

    private static final ResourceLocation channelID = new ResourceLocation(TenshiLib.MODID, "packets");
    private static final SimpleChannel dispatcher = NetworkRegistry.ChannelBuilder.named(channelID)
            .clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> "1").simpleChannel();

    public static void register() {
        int server = PacketRegistrar.registerServerPackets(new PacketRegistrar.ServerPacketRegister() {
            @Override
            public <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, BiConsumer<P, ServerPlayer> handler) {
                dispatcher.registerMessage(index, clss, encoder, decoder, handlerServer(handler));
            }
        }, 0);
        PacketRegistrar.registerClientPackets(new PacketRegistrar.ClientPacketRegister() {
            @Override
            public <P> void registerMessage(int index, ResourceLocation id, Class<P> clss, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, Consumer<P> handler) {
                dispatcher.registerMessage(index, clss, encoder, decoder, handlerClient(handler));
            }
        }, server);
    }

    public static <T> void sendToServer(T message) {
        dispatcher.sendToServer(message);
    }

    public static <T> void sendToClientChecked(T message, ServerPlayer player) {
        if (hasChannel(player))
            dispatcher.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToTracking(T message, Entity e) {
        dispatcher.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> e), message);
    }

    private static boolean hasChannel(ServerPlayer player) {
        ConnectionData data = NetworkHooks.getConnectionData(player.connection.connection);
        return data != null && data.getChannels().containsKey(channelID);
    }

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> handlerServer(BiConsumer<T, ServerPlayer> handler) {
        return (p, ctx) -> {
            ctx.get().enqueueWork(() -> handler.accept(p, ctx.get().getSender()));
            ctx.get().setPacketHandled(true);
        };
    }

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> handlerClient(Consumer<T> handler) {
        return (p, ctx) -> {
            ctx.get().enqueueWork(() -> handler.accept(p));
            ctx.get().setPacketHandled(true);
        };
    }
}
