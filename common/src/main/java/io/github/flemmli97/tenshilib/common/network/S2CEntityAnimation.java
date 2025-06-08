package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.common.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.utils.ArrayUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class S2CEntityAnimation implements CustomPacketPayload {

    public static final Type<S2CEntityAnimation> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "s2c_entity_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CEntityAnimation> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CEntityAnimation decode(RegistryFriendlyByteBuf buf) {
            return new S2CEntityAnimation(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CEntityAnimation pkt) {
            buf.writeInt(pkt.entityID);
            buf.writeInt(pkt.animID);
            buf.writeInt(pkt.startTransition);
            buf.writeInt(pkt.endTransition);
            buf.writeFloat(pkt.start);
        }
    };

    private final int entityID;
    private final int animID;
    private final float start;

    private final int startTransition, endTransition;

    private S2CEntityAnimation(int entityID, int animID, int startTransition, int endTransition, float start) {
        this.entityID = entityID;
        this.animID = animID;
        this.start = start;
        this.startTransition = startTransition;
        this.endTransition = endTransition;
    }

    public static <T extends Entity & AnimatedEntity> S2CEntityAnimation create(T entity, int startTransition, int endTransition, float start) {
        return new S2CEntityAnimation(entity, startTransition, endTransition, start);
    }

    private S2CEntityAnimation(Entity e, int startTransition, int endTransition, float start) {
        this.entityID = e.getId();
        this.start = start;
        this.startTransition = startTransition;
        this.endTransition = endTransition;
        AnimatedEntity entity = (AnimatedEntity) e;
        this.animID = Optional.ofNullable(entity.getAnimationHandler().getAnimation())
                .map(anim -> {
                    int i = 0;
                    for (AnimatedAction a : entity.getAnimationHandler().getAnimations()) {
                        if (a.getID().equals(anim.getID()))
                            break;
                        i++;
                    }
                    if (i < entity.getAnimationHandler().getAnimations().length)
                        return i;
                    TenshiLib.LOGGER.error("This animation is not registered for {}. Registered animations are {} but set animation is {}", e, ArrayUtils.arrayToString(entity.getAnimationHandler().getAnimations(), AnimatedAction::getID), anim.getID());
                    return -1;
                }).orElse(-1);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler {
        public static void handle(S2CEntityAnimation pkt, Player player) {
            ClientHandlers.updateAnim(pkt.entityID, pkt.animID, pkt.startTransition, pkt.endTransition, pkt.start);
        }
    }
}
