package com.flemmli97.tenshilib.api.entity;

import com.flemmli97.tenshilib.common.entity.AnimatedAction;

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
}
