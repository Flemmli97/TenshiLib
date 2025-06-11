package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationDefinitionContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record S2CAnimationDataPacket(
        Map<ResourceLocation, AnimationDefinitionContainer> content) implements CustomPacketPayload {

    public static final Type<S2CAnimationDataPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "s2c_animation_data_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CAnimationDataPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CAnimationDataPacket decode(RegistryFriendlyByteBuf buf) {
            return new S2CAnimationDataPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, AnimationDefinitionContainer.STREAM_CODEC));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CAnimationDataPacket pkt) {
            buf.writeMap(pkt.content, FriendlyByteBuf::writeResourceLocation, AnimationDefinitionContainer.STREAM_CODEC);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CAnimationDataPacket pkt) {
        AnimationDataManager.getInstance().updateFrom(pkt);
    }
}