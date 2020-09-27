package com.flemmli97.tenshilib.api.entity;

import com.flemmli97.tenshilib.common.entity.AnimatedAction;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;


public interface IAnimated {

    @Nullable
    AnimatedAction getAnimation();

    void setAnimation(AnimatedAction anim);

    AnimatedAction[] getAnimations();

    default void tickAnimation() {
        if(this.getAnimation() != null && this.getAnimation().tick())
            this.setAnimation(null);
    }

    static <T extends Entity & IAnimated> void sentToClient(T entity) {
        if(!entity.world.isRemote){
            PacketHandler.sendToTracking(new S2CEntityAnimation<>(entity), entity);
        }
    }
}
