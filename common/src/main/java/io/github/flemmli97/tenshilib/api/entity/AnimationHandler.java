package io.github.flemmli97.tenshilib.api.entity;

import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class AnimationHandler<T extends Entity & IAnimated> {

    public static final int DEFAULT_ADJUST_TIME = 3;

    private AnimatedAction currentAnim;
    private final T entity;
    private final AnimatedAction[] anims;
    private Function<AnimatedAction, Boolean> onAnimationSetFunc;
    private Consumer<AnimatedAction> onAnimationSetCons;
    private Consumer<AnimatedAction> onRunAnimation;
    private int timeSinceLastChange;
    private int delayedCounterMax;
    private int delayedCounter = -1;
    private Runnable delayedAction;

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

    /**
     * Adds a handler for an AnimatedAction.
     */
    public AnimationHandler<T> addActionHandle(Consumer<AnimatedAction> handleAction) {
        this.onRunAnimation = handleAction;
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
        if (this.onAnimationSetFunc != null && this.onAnimationSetFunc.apply(anim))
            return;
        this.timeSinceLastChange = 0;
        if (this.currentAnim != null && this.currentAnim.getFadeTick() > 0) {
            this.delayedAction = () -> {
                this.currentAnim = anim == null ? null : anim.create();
                if (!this.entity.level().isClientSide) {
                    EventCalls.INSTANCE.sendEntityAnimationPacket(this.entity);
                }
            };
            this.delayedCounter = this.currentAnim.getFadeTick();
            this.delayedCounterMax = this.delayedCounter;
        } else {
            this.currentAnim = anim == null ? null : anim.create();
            if (!this.entity.level().isClientSide) {
                EventCalls.INSTANCE.sendEntityAnimationPacket(this.entity);
            }
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

    public void tick() {
        if (this.hasAnimation()) {
            if (this.getAnimation().tick())
                this.setAnimation(null);
            else if (this.onRunAnimation != null)
                this.onRunAnimation.accept(this.getAnimation());
            if (this.delayedAction != null) {
                --this.delayedCounter;
                if (this.delayedCounter < 0) {
                    this.delayedAction.run();
                    this.delayedAction = null;
                    this.delayedCounterMax = 0;
                }
            }
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
        if (this.delayedCounter >= 0)
            return Mth.clamp((this.delayedCounter - partialTicks) / (float) this.delayedCounterMax, 0, 1);
        return Mth.clamp((this.getTimeSinceLastChange() + partialTicks) / adjustTime, 0, 1);
    }
}
