package io.github.flemmli97.tenshilib.common.entity.animated;

import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AnimationHandler<T extends Entity & AnimatedEntity> {

    public static final int DEFAULT_TRANSIT_TIME = 3;
    public static final int FALLBACK_TRANSIT_TIME = -1;

    private final T entity;
    private final AnimationDefinitionContainer definitions;

    private final List<PriorityEntry<Predicate<AnimationDefinition>>> animationChangeListener = new ArrayList<>();
    private ToFloatFunction<AnimationDefinition> animationSpeedHandler;

    private AnimationState currentAnimation, lastAnimation;

    private int timeSinceLastChange = -1;

    public AnimationHandler(T entity) {
        this(entity, AnimationDataManager.getInstance().getAnimation(entity.getType()));
    }

    public AnimationHandler(T entity, AnimationDefinitionContainer definitions) {
        this.entity = entity;
        Objects.requireNonNull(definitions);
        this.definitions = definitions;
    }

    public AnimationHandler<T> withChangeListener(Predicate<AnimationDefinition> onAnimationSet) {
        return this.withChangeListener(-1, onAnimationSet);
    }

    /**
     * Add a listener for whenever animation changes. Return true to prevent the update
     */
    public AnimationHandler<T> withChangeListener(int priority, Predicate<AnimationDefinition> onAnimationSet) {
        if (priority == -1) {
            this.animationChangeListener.add(new PriorityEntry<>(priority, onAnimationSet));
        } else {
            this.animationChangeListener.add(new PriorityEntry<>(priority, onAnimationSet));
            this.animationChangeListener.sort(Comparator.reverseOrder());
        }
        return this;
    }

    public AnimationHandler<T> withAnimationSpeedHandler(ToFloatFunction<AnimationDefinition> animationSpeedHandler) {
        this.animationSpeedHandler = animationSpeedHandler;
        return this;
    }

    public T getEntity() {
        return this.entity;
    }

    @Nullable
    public AnimationState getAnimation() {
        return this.currentAnimation;
    }

    public void runIfAnimation(String id, Consumer<AnimationState> anim) {
        if (this.isCurrent(id)) {
            anim.accept(this.getAnimation());
        }
    }

    public void runIfNotNull(Consumer<AnimationState> cons) {
        if (this.currentAnimation != null)
            cons.accept(this.currentAnimation);
    }

    public boolean hasAnimation() {
        return this.currentAnimation != null;
    }

    public void setAnimation(AnimationDefinition anim) {
        this.setAnimation(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, AnimationHandler.FALLBACK_TRANSIT_TIME, 0);
    }

    /**
     * @param anim            The animation to set. Or null for no animation
     * @param startTransition Duration in ticks to transition INTO this animation. -1 for fallback
     * @param endTransition   Duration in ticks to transition OUT of this animation. -1 for fallback
     * @param offset          Start the animation with the given offset
     */
    public void setAnimation(AnimationDefinition anim, int startTransition, int endTransition, double offset) {
        for (PriorityEntry<Predicate<AnimationDefinition>> listener : this.animationChangeListener) {
            if (listener.val().test(anim))
                return;
        }
        if (this.currentAnimation != null) {
            this.lastAnimation = this.currentAnimation;
            this.timeSinceLastChange = 0;
            if (anim != null) {
                startTransition = startTransition > 0 ? startTransition : this.lastAnimation.getEndTransitionTime();
                this.lastAnimation = AnimationState.create(anim, this.currentAnimation.getStartTransition(),
                        startTransition, this.currentAnimation.getTick(1),
                        this.currentAnimation.getSpeed());
            }
        } else if (this.lastAnimation != null && anim != null) {
            this.lastAnimation = AnimationState.create(anim, this.lastAnimation.getStartTransition(),
                    startTransition + this.timeSinceLastChange, this.lastAnimation.getTick(1),
                    this.lastAnimation.getSpeed());
        }
        this.currentAnimation = anim == null ? null : AnimationState.create(anim, startTransition, endTransition,
                offset, this.animationSpeedHandler == null ? 1 : this.animationSpeedHandler.apply(anim));
        if (!this.entity.level().isClientSide) {
            LoaderNetwork.INSTANCE.sendToTracking(S2CEntityAnimation.create(this.entity, startTransition, endTransition, offset), this.entity);
        }
    }

    public AnimationDefinitionContainer getAnimations() {
        return this.definitions;
    }

    public boolean isCurrent(AnimationDefinition... anims) {
        if (this.getAnimation() == null)
            return false;
        for (AnimationDefinition action : anims)
            if (this.getAnimation().getID().equals(action.id()))
                return true;
        return false;
    }

    public boolean isCurrent(String... ids) {
        if (this.getAnimation() == null)
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

    public AnimationState getLastAnimation() {
        return this.lastAnimation;
    }

    public void tick() {
        this.timeSinceLastChange++;
        if (this.lastAnimation != null && this.timeSinceLastChange > this.lastAnimation.getEndTransitionTime()) {
            this.lastAnimation = null;
        }
        if (this.hasAnimation()) {
            if (this.getAnimation().tick())
                this.setAnimation(null);
        }
    }

    /**
     * Skip the animation to the end
     */
    public void finishAnimation() {
        AnimationState anim = this.getAnimation();
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
        return (float) this.currentAnimation.getStartTransitionProgress(partialTicks);
    }

    public float getLastTransitionProgress(float partialTicks) {
        if (this.lastAnimation == null) {
            return 0;
        }
        return 1 - Mth.clamp((this.getTimeSinceLastChange() - 1 + partialTicks) / this.lastAnimation.getEndTransitionTime(), 0, 1);
    }

    private record PriorityEntry<T>(int priority, T val) implements Comparable<PriorityEntry<T>> {

        @Override
        public int compareTo(@NotNull AnimationHandler.PriorityEntry<T> other) {
            return Integer.compare(this.priority(), other.priority());
        }
    }
}
