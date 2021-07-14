package com.flemmli97.tenshilib.common.network;

import com.flemmli97.tenshilib.api.entity.AnimatedAction;
import com.flemmli97.tenshilib.api.entity.IAnimated;
import com.flemmli97.tenshilib.client.events.handler.ClientPacketHandlers;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CEntityAnimation<T extends Entity & IAnimated<T>> {

    private final int entityID;
    private final int animID;

    private S2CEntityAnimation(int entityID, int animID) {
        this.entityID = entityID;
        this.animID = animID;
    }

    public S2CEntityAnimation(T entity) {
        this.entityID = entity.getEntityId();
        this.animID = entity.getAnimationHandler().getAnimation()
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
                        return i;
                    }
                }).orElse(-2);
    }

    public static <T extends Entity & IAnimated<T>> S2CEntityAnimation<T> fromBytes(PacketBuffer buf) {
        return new S2CEntityAnimation<>(buf.readInt(), buf.readInt());
    }

    public static <T extends Entity & IAnimated<T>> void toBytes(S2CEntityAnimation<T> pkt, PacketBuffer buf) {
        buf.writeInt(pkt.entityID);
        buf.writeInt(pkt.animID);
    }

    public static <T extends Entity & IAnimated<T>> void handlePacket(S2CEntityAnimation<T> pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.updateAnim(pkt.entityID, pkt.animID)));
        ctx.get().setPacketHandled(true);
    }
}
