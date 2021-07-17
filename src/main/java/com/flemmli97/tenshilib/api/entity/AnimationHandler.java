package com.flemmli97.tenshilib.api.entity;

import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class AnimationHandler<T extends Entity & IAnimated> {

    private Optional<AnimatedAction> currentAnim = Optional.empty();
    private final T entity;
    private final AnimatedAction[] anims;
    private Function<AnimatedAction, Boolean> onAnimationSetFunc;
    private Consumer<AnimatedAction> onAnimationSetCons;

    public AnimationHandler(T entity, AnimatedAction[] anims) {
        this.entity = entity;
        Objects.requireNonNull(anims);
        this.anims = anims;
    }

    public AnimationHandler<T> setAnimationChangeFunc(Function<AnimatedAction, Boolean> onAnimationSet) {
        this.onAnimationSetFunc = onAnimationSet;
        return this;
    }

    public AnimationHandler<T> setAnimationChangeCons(Consumer<AnimatedAction> onAnimationSetCons) {
        this.onAnimationSetCons = onAnimationSetCons;
        return this;
    }

    @Nullable
    public Optional<AnimatedAction> getAnimation() {
        return this.currentAnim;
    }

    public boolean hasAnimation() {
        return this.currentAnim.isPresent();
    }

    public void setAnimation(AnimatedAction anim) {
        if (this.onAnimationSetCons != null)
            this.onAnimationSetCons.accept(anim);
        if (this.onAnimationSetFunc != null && this.onAnimationSetFunc.apply(anim))
            return;
        this.currentAnim = Optional.ofNullable(anim == null ? null : anim.create());
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
