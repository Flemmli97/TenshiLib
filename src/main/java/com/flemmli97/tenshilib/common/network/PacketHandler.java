package com.flemmli97.tenshilib.common.network;

import com.flemmli97.tenshilib.TenshiLib;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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

    public static <T> void sendToClient(T message, ServerPlayerEntity player) {
        dispatcher.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToTracking(T message, Entity e){
        dispatcher.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->e), message);
    }
}
