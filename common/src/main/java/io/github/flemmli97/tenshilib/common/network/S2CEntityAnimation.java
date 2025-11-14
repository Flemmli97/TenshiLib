package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class S2CEntityAnimation implements CustomPacketPayload {

    public static final Type<S2CEntityAnimation> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "s2c_entity_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CEntityAnimation> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CEntityAnimation decode(RegistryFriendlyByteBuf buf) {
            return new S2CEntityAnimation(buf.readInt(), buf.readBoolean() ? AnimationState.SyncableState.STREAM_CODEC.decode(buf) : null);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CEntityAnimation pkt) {
            buf.writeInt(pkt.entityID);
            buf.writeBoolean(pkt.state != null);
            if (pkt.state != null) {
                AnimationState.SyncableState.STREAM_CODEC.encode(buf, pkt.state);
            }
        }
    };

    private final int entityID;
    private final AnimationState.SyncableState state;

    private S2CEntityAnimation(int entityID, @Nullable AnimationState.SyncableState state) {
        this.entityID = entityID;
        this.state = state;
    }

    public static <T extends Entity & AnimatedEntity> S2CEntityAnimation create(T entity) {
        AnimationState state = entity.getAnimationHandler().getAnimation();
        return new S2CEntityAnimation(entity, state != null ? state.forSync() : null);
    }

    public static <T extends Entity & AnimatedEntity> S2CEntityAnimation create(T entity, double offset) {
        AnimationState state = entity.getAnimationHandler().getAnimation();
        return new S2CEntityAnimation(entity, state != null ? state.forSync().withOffset(offset) : null);
    }

    private S2CEntityAnimation(Entity entity, @Nullable AnimationState.SyncableState state) {
        this(entity.getId(), state);
        AnimatedEntity animated = (AnimatedEntity) entity;
        if (state != null && animated.getAnimationHandler().getAnimations().get(state.id()) == null) {
            TenshiLib.LOGGER.error("This animation is not registered for {}. Registered animations are {} but set animation is {}", entity, animated.getAnimationHandler().getAnimations().all(), state.id());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CEntityAnimation pkt, Player player) {
        Entity e = player.level().getEntity(pkt.entityID);
        if (e instanceof AnimatedEntity anim) {
            if (pkt.state == null) {
                anim.getAnimationHandler().setAnimationDef(null);
            } else {
                anim.getAnimationHandler().setAnimation(anim.getAnimationHandler().getAnimations().get(pkt.state.id()),
                        pkt.state.startTransition(), pkt.state.endTransition(), pkt.state.offset(), pkt.state.speed());
            }
        }
    }
}
