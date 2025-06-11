package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class S2CEntityAnimation implements CustomPacketPayload {

    public static final Type<S2CEntityAnimation> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "s2c_entity_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CEntityAnimation> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CEntityAnimation decode(RegistryFriendlyByteBuf buf) {
            return new S2CEntityAnimation(buf.readInt(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readDouble());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CEntityAnimation pkt) {
            buf.writeInt(pkt.entityID);
            buf.writeUtf(pkt.animID);
            buf.writeInt(pkt.startTransition);
            buf.writeInt(pkt.endTransition);
            buf.writeDouble(pkt.start);
        }
    };

    private final int entityID;
    private final String animID;
    private final double start;

    private final int startTransition, endTransition;

    private S2CEntityAnimation(int entityID, String animID, int startTransition, int endTransition, double start) {
        this.entityID = entityID;
        this.animID = animID;
        this.start = start;
        this.startTransition = startTransition;
        this.endTransition = endTransition;
    }

    public static <T extends Entity & AnimatedEntity> S2CEntityAnimation create(T entity, int startTransition, int endTransition, double start) {
        return new S2CEntityAnimation(entity, startTransition, endTransition, start);
    }

    private S2CEntityAnimation(Entity e, int startTransition, int endTransition, double start) {
        this.entityID = e.getId();
        this.start = start;
        this.startTransition = startTransition;
        this.endTransition = endTransition;
        AnimatedEntity entity = (AnimatedEntity) e;
        AnimationState state = entity.getAnimationHandler().getAnimation();
        this.animID = state != null ? state.getID() : "";
        if (!this.animID.isEmpty() && entity.getAnimationHandler().getAnimations().get(this.animID) == null)
            TenshiLib.LOGGER.error("This animation is not registered for {}. Registered animations are {} but set animation is {}", e, entity.getAnimationHandler().getAnimations().all(), this.animID);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CEntityAnimation pkt) {
        ClientHandlers.updateAnim(pkt.entityID, pkt.animID, pkt.startTransition, pkt.endTransition, pkt.start);
    }
}
