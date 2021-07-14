package com.flemmli97.tenshilib.api.entity;

import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.util.Optional;

public class AnimationHandler<T extends Entity & IAnimated<T>> {

    private AnimatedAction currentAnim;
    private final T entity;
    private final AnimatedAction[] anims;

    public AnimationHandler(T entity, AnimatedAction[] anims) {
        this.entity = entity;
        this.anims = anims;
    }

    @Nullable
    public Optional<AnimatedAction> getAnimation() {
        return Optional.ofNullable(this.currentAnim);
    }

    public void setAnimation(AnimatedAction anim) {
        this.currentAnim = anim == null ? null : anim.create();
        if (!this.entity.world.isRemote) {
            PacketHandler.sendToTracking(new S2CEntityAnimation<>(this.entity), this.entity);
        }
    }

    public AnimatedAction[] getAnimations() {
        return this.anims;
    }

    public boolean isCurrentAnim(String id) {
        return this.getAnimation().map(anim -> anim.getID().equals(id)).orElse(false);
    }

    public boolean isCurrentAnim(String... ids) {
        return this.getAnimation().map(anim -> {
            for (String id : ids) {
                if (anim.getID().equals(id))
                    return true;
            }
            return false;
        }).orElse(false);
    }

    public void tick() {
        if (this.getAnimation().map(AnimatedAction::tick).orElse(false))
            this.setAnimation(null);
    }
}
