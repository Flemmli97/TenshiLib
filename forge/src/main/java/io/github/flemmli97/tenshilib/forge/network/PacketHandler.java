package io.github.flemmli97.tenshilib.forge.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final SimpleChannel dispatcher = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(TenshiLib.MODID, "packets"))
            .clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> "1").simpleChannel();

    public static void register() {
        int id = 0;
        dispatcher.registerMessage(id++, C2SPacketHit.class, C2SPacketHit::toBytes, C2SPacketHit::fromBytes, C2SPacketHit::handlePacket);
        dispatcher.registerMessage(id++, S2CEntityAnimation.class, S2CEntityAnimation::toBytes, S2CEntityAnimation::fromBytes, S2CEntityAnimation::handlePacket);
    }

    public static <T> void sendToServer(T message) {
        dispatcher.sendToServer(message);
    }

    public static <T> void sendToClient(T message, ServerPlayer player) {
        dispatcher.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToTracking(T message, Entity e) {
        dispatcher.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> e), message);
    }
}
