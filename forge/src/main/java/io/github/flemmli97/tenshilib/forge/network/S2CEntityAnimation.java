package io.github.flemmli97.tenshilib.forge.network;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class S2CEntityAnimation {

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

    public static S2CEntityAnimation fromBytes(FriendlyByteBuf buf) {
        return new S2CEntityAnimation(buf.readInt(), buf.readInt());
    }

    public static void toBytes(S2CEntityAnimation pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityID);
        buf.writeInt(pkt.animID);
    }

    public static void handlePacket(S2CEntityAnimation pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandlers.updateAnim(pkt.entityID, pkt.animID)));
        ctx.get().setPacketHandled(true);
    }
}
