package io.github.flemmli97.tenshilib.common.entity.animated;

import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AnimationHandler<T extends Entity> {

    public static final int DEFAULT_TRANSIT_TIME = 3;
    public static final int FALLBACK_TRANSIT_TIME = -1;

    private final T entity;
    private final AnimationDefinitionContainer definitions;

    private final List<PriorityEntry<Predicate<AnimationDefinition>>> animationChangeListener = new ArrayList<>();
    private SpeedHandler animationSpeedHandler;

    private AnimationState currentAnimation, lastAnimation;

    private int timeSinceLastChange = -1;

    public AnimationHandler(T entity, AnimationDefinitionContainer defaulted) {
        this(entity, BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), defaulted);
    }

    public AnimationHandler(T entity, ResourceLocation id, AnimationDefinitionContainer defaulted) {
        this.entity = entity;
        this.definitions = AnimationDataManager.getInstance().getAnimation(id, defaulted);
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

    public AnimationHandler<T> withAnimationSpeedHandler(SpeedHandler animationSpeedHandler) {
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

    public void runIfAnimation(String id, Consumer<AnimationState> consumer) {
        if (this.isCurrent(id)) {
            consumer.accept(this.getAnimation());
        }
    }

    public void runIfNotNull(Consumer<AnimationState> consumer) {
        if (this.currentAnimation != null)
            consumer.accept(this.currentAnimation);
    }

    public boolean hasAnimation() {
        return this.currentAnimation != null;
    }

    public AnimationDefinition get(@Nullable String name) {
        if (name == null)
            return null;
        AnimationDefinition definition = this.definitions.get(name);
        if (definition == null)
            throw new IllegalStateException("No such animation definition " + name);
        return definition;
    }

    public AnimationState createDefaulted(String name) {
        return AnimationState.create(this.get(name));
    }

    public void setAnimation(@Nullable String name) {
        this.setAnimationDef(this.get(name));
    }

    public void setAnimationDef(@Nullable AnimationDefinition animation) {
        this.setAnimation(animation, AnimationHandler.FALLBACK_TRANSIT_TIME, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, 1);
    }

    /**
     * @param animation       The animation to set. Or null for no animation
     * @param startTransition Duration in ticks to transition INTO this animation. -1 for fallback
     * @param endTransition   Duration in ticks to transition OUT of this animation. -1 for fallback
     * @param offset          Start the animation with the given offset
     */
    public void setAnimation(@Nullable AnimationDefinition animation, int startTransition, int endTransition, double offset, double speed) {
        for (PriorityEntry<Predicate<AnimationDefinition>> listener : this.animationChangeListener) {
            if (listener.val().test(animation))
                return;
        }
        if (this.currentAnimation != null) {
            this.lastAnimation = this.currentAnimation;
            this.timeSinceLastChange = 0;
            if (animation != null) {
                startTransition = startTransition > 0 ? startTransition : this.lastAnimation.getEndTransitionTime();
                this.lastAnimation = AnimationState.create(this.currentAnimation.definition(), this.currentAnimation.getStartTransition(),
                        startTransition, this.currentAnimation.getTick(1),
                        this.currentAnimation.getSpeed());
            }
        } else if (this.lastAnimation != null && animation != null) {
            this.lastAnimation = AnimationState.create(this.lastAnimation.definition(), this.lastAnimation.getStartTransition(),
                    startTransition + this.timeSinceLastChange, this.lastAnimation.getTick(1),
                    this.lastAnimation.getSpeed());
        }
        this.currentAnimation = animation == null ? null : AnimationState.create(animation, startTransition, endTransition,
                offset, this.animationSpeedHandler == null ? speed : this.animationSpeedHandler.getSpeed(speed, animation));
        if (!this.getEntity().level().isClientSide) {
            this.syncToClient();
        }
    }

    protected void syncToClient() {
        if (this.getEntity() instanceof AnimatedEntity) {
            LoaderNetwork.INSTANCE.sendToTracking(S2CEntityAnimation.create((Entity & AnimatedEntity) this.getEntity()), this.getEntity());
        }
    }

    public AnimationDefinitionContainer getAnimations() {
        return this.definitions;
    }

    public boolean isCurrent(AnimationDefinition... others) {
        if (this.getAnimation() == null)
            return false;
        return this.getAnimation().is(others);
    }

    public boolean isCurrent(String... others) {
        if (this.getAnimation() == null)
            return false;
        return this.getAnimation().is(others);
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
                this.setAnimationDef(null);
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
                this.setAnimationDef(null);
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
        if (this.lastAnimation == null || this.lastAnimation.getEndTransitionTime() <= 0) {
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

    public interface SpeedHandler {

        double getSpeed(double current, AnimationDefinition animation);
    }
}
