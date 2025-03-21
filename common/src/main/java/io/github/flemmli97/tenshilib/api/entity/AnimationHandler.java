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

    public static final int DEFAULT_TRANSIT_TIME = 3;

    private final T entity;
    private final AnimatedAction[] anims;

    private Predicate<AnimatedAction> animationChangeListener;
    private Consumer<AnimatedAction> onRunAnimation;
    private ToFloatFunction<AnimatedAction> animationSpeedHandler;

    private AnimatedAction currentAnimation, lastAnimation;

    private int timeSinceLastChange = -1;

    public AnimationHandler(T entity, AnimatedAction[] anims) {
        this.entity = entity;
        Objects.requireNonNull(anims);
        this.anims = anims;
    }

    /**
     * Add a listener for whenever animation changes. Return true to prevent the update
     */
    public AnimationHandler<T> withChangeListener(Predicate<AnimatedAction> onAnimationSet) {
        this.animationChangeListener = onAnimationSet;
        return this;
    }

    /**
     * Adds a handler for an AnimatedAction.
     */
    public AnimationHandler<T> withHandle(Consumer<AnimatedAction> handleAction) {
        this.onRunAnimation = handleAction;
        return this;
    }

    public AnimationHandler<T> withAnimationSpeedHandler(ToFloatFunction<AnimatedAction> animationSpeedHandler) {
        this.animationSpeedHandler = animationSpeedHandler;
        return this;
    }

    @Nullable
    public AnimatedAction getAnimation() {
        return this.currentAnimation;
    }

    public void runIfAnimation(String id, Consumer<AnimatedAction> anim) {
        if (this.isCurrent(id)) {
            anim.accept(this.getAnimation());
        }
    }

    public void runIfNotNull(Consumer<AnimatedAction> cons) {
        if (this.currentAnimation != null)
            cons.accept(this.currentAnimation);
    }

    public boolean hasAnimation() {
        return this.currentAnimation != null;
    }

    public void setAnimation(AnimatedAction anim) {
        this.setAnimation(anim, -1, -1, 0);
    }

    /**
     * @param anim            The animation to set. Or null for no animation
     * @param startTransition Duration in ticks to transition INTO this animation
     * @param endTransition   Duration in ticks to transition OUT of this animation
     * @param offset          Start the animation with the given offset
     */
    public void setAnimation(AnimatedAction anim, int startTransition, int endTransition, float offset) {
        if (this.animationChangeListener != null && this.animationChangeListener.test(anim))
            return;
        if (this.currentAnimation != null) {
            this.lastAnimation = this.currentAnimation;
            this.timeSinceLastChange = 0;
            // Animation is getting replaced. Transition time would then be equal to the replaced end time
            if (anim != null) {
                startTransition = this.lastAnimation.getEndTransitionTime() > 0 ? this.lastAnimation.getEndTransitionTime() : startTransition;
                this.lastAnimation = this.lastAnimation.create(this.currentAnimation.getStartTransition(),
                        startTransition, this.currentAnimation.getTick(1),
                        this.currentAnimation.getSpeed());
            }
        } else if (this.lastAnimation != null && this.timeSinceLastChange < this.lastAnimation.getEndTransitionTime()) {
            // Still transitioning from old animation. Remaining time is new start transition
            startTransition = Math.max(0, this.lastAnimation.getEndTransitionTime() - this.timeSinceLastChange);
        }
        this.currentAnimation = anim == null ? null : anim.create(startTransition, endTransition,
                offset, this.animationSpeedHandler == null ? anim.getSpeed() : this.animationSpeedHandler.apply(anim));
        if (!this.entity.level.isClientSide) {
            EventCalls.INSTANCE.sendEntityAnimationPacket(this.entity, startTransition, endTransition, offset);
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

    public AnimatedAction getLastAnimation() {
        return this.lastAnimation;
    }

    public void tick() {
        if (this.timeSinceLastChange >= 0)
            this.timeSinceLastChange++;
        if (this.hasAnimation()) {
            if (this.getAnimation().tick())
                this.setAnimation(null);
            else if (this.onRunAnimation != null)
                this.onRunAnimation.accept(this.getAnimation());
        }
    }

    /**
     * Skip the animation to the end
     */
    public void finishAnimation() {
        AnimatedAction anim = this.getAnimation();
        if (anim != null) {
            while (!anim.done(1))
                anim.tick();
            if (anim.shouldRunOut()) {
                this.setAnimation(null);
            } else {
                this.timeSinceLastChange = Mth.ceil(anim.getLength());
            }
        }
    }

    public float getCurrentTransitionProgress(float partialTicks) {
        if (this.currentAnimation == null) {
            return 1;
        }
        return this.currentAnimation.getStartTransitionProgress(partialTicks);
    }

    public float getLastTransitionProgress(float partialTicks) {
        if (this.lastAnimation == null) {
            return 0;
        }
        return 1 - Mth.clamp((this.getTimeSinceLastChange() - 1 + partialTicks) / this.lastAnimation.getEndTransitionTime(), 0, 1);
    }
}
