package io.github.flemmli97.tenshilib.api.entity;

import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AnimationHandler<T extends Entity & IAnimated> {

    public static final int DEFAULT_ADJUST_TIME = 3;

    private AnimatedAction currentAnim, lastAnim;
    private final T entity;
    private final AnimatedAction[] anims;
    private Predicate<AnimatedAction> onAnimationSetFunc;
    private Consumer<AnimatedAction> onAnimationSetCons;
    private Consumer<AnimatedAction> onRunAnimation;
    private ToFloatFunction<AnimatedAction> animationSpeedHandler;
    private int timeSinceLastChange;

    public AnimationHandler(T entity, AnimatedAction[] anims) {
        this.entity = entity;
        Objects.requireNonNull(anims);
        this.anims = anims;
    }

    public AnimationHandler<T> setAnimationChangeFunc(Predicate<AnimatedAction> onAnimationSet) {
        this.onAnimationSetFunc = onAnimationSet;
        return this;
    }

    public AnimationHandler<T> setAnimationChangeCons(Consumer<AnimatedAction> onAnimationSetCons) {
        this.onAnimationSetCons = onAnimationSetCons;
        return this;
    }

    /**
     * Adds a handler for an AnimatedAction.
     */
    public AnimationHandler<T> addActionHandle(Consumer<AnimatedAction> handleAction) {
        this.onRunAnimation = handleAction;
        return this;
    }

    public AnimationHandler<T> setAnimationSpeedHandler(ToFloatFunction<AnimatedAction> animationSpeedHandler) {
        this.animationSpeedHandler = animationSpeedHandler;
        return this;
    }

    @Nullable
    public AnimatedAction getAnimation() {
        return this.currentAnim;
    }

    public void runIfAnimation(String id, Consumer<AnimatedAction> anim) {
        if (this.isCurrent(id)) {
            anim.accept(this.getAnimation());
        }
    }

    public void runIfNotNull(Consumer<AnimatedAction> cons) {
        if (this.currentAnim != null)
            cons.accept(this.currentAnim);
    }

    public boolean hasAnimation() {
        return this.currentAnim != null;
    }

    public void setAnimation(AnimatedAction anim) {
        if (this.onAnimationSetCons != null)
            this.onAnimationSetCons.accept(anim);
        if (this.onAnimationSetFunc != null && this.onAnimationSetFunc.test(anim))
            return;
        this.lastAnim = this.currentAnim;
        this.timeSinceLastChange = 0;
        this.currentAnim = anim == null ? null : anim.create(this.animationSpeedHandler == null ? anim.getSpeed() : this.animationSpeedHandler.apply(anim));
        if (!this.entity.level.isClientSide) {
            EventCalls.INSTANCE.sendEntityAnimationPacket(this.entity);
        }
    }

    public AnimatedAction[] getAnimations() {
        return this.anims;
    }

    public boolean isCurrent(AnimatedAction... anims) {
        for (AnimatedAction action : anims)
            if (action.is(this.getAnimation()))
                return true;
        return false;
    }

    public boolean isCurrent(String... ids) {
        if (!this.hasAnimation())
            return false;
        for (String id : ids)
            if (this.getAnimation().getID().equals(id))
                return true;
        return false;
    }

    /**
     * Gets the time in ticks since the animation last changed.
     * To be used in interpolating between animations.
     */
    public int getTimeSinceLastChange() {
        return this.timeSinceLastChange;
    }

    public AnimatedAction getLastAnim() {
        return this.lastAnim;
    }

    public void tick() {
        if (this.hasAnimation()) {
            if (this.getAnimation().tick())
                this.setAnimation(null);
            else if (this.onRunAnimation != null)
                this.onRunAnimation.accept(this.getAnimation());
        }
        this.timeSinceLastChange++;
    }

    /**
     * Skip the animation to the end
     */
    public void finishAnimation() {
        AnimatedAction anim = this.getAnimation();
        if (anim != null) {
            while (anim.getTick() < anim.getLength())
                anim.tick();
            if (anim.shouldRunOut()) {
                this.setAnimation(null);
            } else {
                this.timeSinceLastChange = anim.getLength();
            }
        }
    }

    /**
     * @return A value usable for interpolating between different animations using a default adjust time of 3 ticks
     */
    public float getInterpolatedAnimationVal(float partialTicks) {
        return this.getInterpolatedAnimationVal(partialTicks, DEFAULT_ADJUST_TIME);
    }

    public float getInterpolatedAnimationVal(float partialTicks, float adjustTime) {
        return Mth.clamp((this.getTimeSinceLastChange() + partialTicks) / adjustTime, 0, 1);
    }
}
