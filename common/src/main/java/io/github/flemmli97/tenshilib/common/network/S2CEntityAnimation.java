package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
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
            return new S2CEntityAnimation(buf.readInt(), buf.readInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CEntityAnimation pkt) {
            buf.writeInt(pkt.entityID);
            buf.writeInt(pkt.animID);
        }
    };

    private final int entityID;
    private final int animID;

    private S2CEntityAnimation(int entityID, int animID) {
        this.entityID = entityID;
        this.animID = animID;
    }

    public static <T extends Entity & IAnimated> S2CEntityAnimation create(T entity) {
        return new S2CEntityAnimation(entity);
    }

    private S2CEntityAnimation(Entity e) {
        this.entityID = e.getId();
        IAnimated entity = (IAnimated) e;
        this.animID = Optional.ofNullable(entity.getAnimationHandler().getAnimation())
                .map(anim -> {
                    if (anim == AnimatedAction.VANILLA_ATTACK)
                        return -1;
                    else {
                        int i = 0;
                        for (AnimatedAction a : entity.getAnimationHandler().getAnimations()) {
                            if (a.getID().equals(anim.getID()))
                                break;
                            i++;
                        }
                        if (i < entity.getAnimationHandler().getAnimations().length)
                            return i;
                        TenshiLib.LOGGER.error("This animation is not registered for {}. Registered animations are {} but set animation is {}", e, ArrayUtils.arrayToString(entity.getAnimationHandler().getAnimations(), AnimatedAction::getID), anim.getID());
                        return -2;
                    }
                }).orElse(-2);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler {
        public static void handlePacket(S2CEntityAnimation pkt, Player player) {
            ClientHandlers.updateAnim(pkt.entityID, pkt.animID);
        }
    }
}
