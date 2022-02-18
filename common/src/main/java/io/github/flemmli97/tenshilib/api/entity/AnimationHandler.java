package io.github.flemmli97.tenshilib.api.entity;

import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class AnimationHandler<T extends Entity & IAnimated> {

    private AnimatedAction currentAnim;
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
    public AnimatedAction getAnimation() {
        return this.currentAnim;
    }

    public void runIfAnimation(String id, Consumer<AnimatedAction> anim) {
        if (this.isCurrentAnim(id)) {
            anim.accept(this.getAnimation());
        }
    }

    public boolean hasAnimation() {
        return this.currentAnim != null;
    }

    public void setAnimation(AnimatedAction anim) {
        if (this.onAnimationSetCons != null)
            this.onAnimationSetCons.accept(anim);
        if (this.onAnimationSetFunc != null && this.onAnimationSetFunc.apply(anim))
            return;
        this.currentAnim = anim == null ? null : anim.create();
        if (!this.entity.level.isClientSide) {
            EventCalls.instance().sendEntityAnimationPacket(this.entity);
        }
    }

    public AnimatedAction[] getAnimations() {
        return this.anims;
    }

    public boolean isCurrentAnim(String id) {
        return this.hasAnimation() && this.getAnimation().getID().equals(id);
    }

    public boolean isCurrentAnim(String... ids) {
        if (!this.hasAnimation())
            return false;
        for (String id : ids)
            if (this.getAnimation().getID().equals(id))
                return true;
        return false;
    }

    public void tick() {
        if (this.hasAnimation() && this.getAnimation().tick())
            this.setAnimation(null);
    }
}
