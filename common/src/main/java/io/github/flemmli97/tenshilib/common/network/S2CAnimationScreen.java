package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.common.entity.AnimatedEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class S2CAnimationScreen implements CustomPacketPayload {

    public static final Type<S2CAnimationScreen> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "s2c_animation_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CAnimationScreen> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CAnimationScreen decode(RegistryFriendlyByteBuf buf) {
            return new S2CAnimationScreen(buf.readEnum(InteractionHand.class), buf.readInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CAnimationScreen pkt) {
            buf.writeEnum(pkt.hand);
            buf.writeInt(pkt.entity);
        }
    };

    private final InteractionHand hand;
    private final int entity;

    public S2CAnimationScreen(InteractionHand hand, LivingEntity entity) {
        this.hand = hand;
        this.entity = entity.getId();
    }

    private S2CAnimationScreen(InteractionHand hand, int entity) {
        this.hand = hand;
        this.entity = entity;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler {
        public static void handle(S2CAnimationScreen pkt, Player player) {
            if (player == null)
                return;
            Entity entity = player.level().getEntity(pkt.entity);
            if (!(entity instanceof LivingEntity) || !(entity instanceof AnimatedEntity))
                return;
            ClientHandlers.openAnimationGui((LivingEntity & AnimatedEntity) entity, pkt.hand);
        }
    }
}