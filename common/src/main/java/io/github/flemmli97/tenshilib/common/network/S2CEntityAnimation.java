package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public class S2CEntityAnimation implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "s2c_entity_animation");

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
                    if (anim == AnimatedAction.vanillaAttack)
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
                        return -2;
                    }
                }).orElse(-2);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeInt(this.animID);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public static S2CEntityAnimation fromBytes(FriendlyByteBuf buf) {
        return new S2CEntityAnimation(buf.readInt(), buf.readInt());
    }

    public static void handlePacket(S2CEntityAnimation pkt) {
        ClientHandlers.updateAnim(pkt.entityID, pkt.animID);
    }
}
