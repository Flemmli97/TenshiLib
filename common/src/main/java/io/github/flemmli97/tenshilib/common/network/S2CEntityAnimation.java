package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.common.utils.ArrayUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public class S2CEntityAnimation implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "s2c_entity_animation");

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

    public static <T extends Entity & IAnimated> S2CEntityAnimation create(T entity, int startTransition, int endTransition, float start) {
        return new S2CEntityAnimation(entity, startTransition, endTransition, start);
    }

    private S2CEntityAnimation(Entity e, int startTransition, int endTransition, float start) {
        this.entityID = e.getId();
        this.start = start;
        this.startTransition = startTransition;
        this.endTransition = endTransition;
        IAnimated entity = (IAnimated) e;
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
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeInt(this.animID);
        buf.writeInt(this.startTransition);
        buf.writeInt(this.endTransition);
        buf.writeFloat(this.start);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public static S2CEntityAnimation fromBytes(FriendlyByteBuf buf) {
        return new S2CEntityAnimation(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat());
    }

    public static class Handler {
        public static void handlePacket(S2CEntityAnimation pkt) {
            ClientHandlers.updateAnim(pkt.entityID, pkt.animID, pkt.startTransition, pkt.endTransition, pkt.start);
        }
    }
}
