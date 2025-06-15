package io.github.flemmli97.tenshilib.common.entity.ai.brain.data;

import com.google.common.collect.ImmutableList;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationHandler;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AnimationPlayHolder<T extends Mob & AnimatedEntity> {

    private final String animation;
    private final List<ChainedAnimations<T>> chainedActions;
    private final float chainChance;

    public AnimationPlayHolder(String animation) {
        this(animation, null, 1);
    }

    private AnimationPlayHolder(String animation, List<ChainedAnimations<T>> chainedActions, float chainChance) {
        this.animation = animation;
        this.chainedActions = chainedActions;
        this.chainChance = chainChance;
    }

    public static <T extends Mob & AnimatedEntity> Builder<T> builder(String animation) {
        return new Builder<>(animation);
    }

    public String animation() {
        return this.animation;
    }

    /**
     * Returns potential chained animation to play.
     * Chained animation will play immediately after the previous animation finished
     */
    @Nullable
    public List<AnimationHolder> get(T mob) {
        if (this.chainedActions == null)
            return null;
        if (mob.getRandom().nextFloat() >= this.chainChance)
            return null;
        List<ChainedAnimations<T>> filtered = this.chainedActions.stream().filter(d -> d.predicate().test(mob)).toList();
        return WeightedRandom.getRandomItem(mob.getRandom(), filtered).map(ChainedAnimations::chains).orElse(null);
    }

    public static class Builder<T extends Mob & AnimatedEntity> {

        private final String animation;
        private final List<ChainedAnimations.ChainListBuilder<T>> anims = new ArrayList<>();
        private float chance = 1;

        private Builder(String animation) {
            this.animation = animation;
        }

        public Builder<T> chainChance(float chance) {
            this.chance = chance;
            return this;
        }

        public Builder<T> start(String anim) {
            return this.start(anim, e -> true);
        }

        public Builder<T> start(String anim, Predicate<T> predicate) {
            return this.start(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, 1, predicate);
        }

        public Builder<T> start(String anim, int transitionTime, float offset, int weight) {
            return this.start(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, 1, e -> true);
        }

        /**
         * Starts a new chain
         *
         * @param offset    Offset in seconds
         * @param predicate Condition required
         */
        public Builder<T> start(String anim, int transitionTime, float offset, int weight, Predicate<T> predicate) {
            this.anims.add(new ChainedAnimations.ChainListBuilder<>(new AnimationHolder(anim, transitionTime, offset * 20),
                    weight, predicate));
            return this;
        }

        public Builder<T> chain(String anim) {
            return this.chain(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, 0);
        }

        /**
         * Appends the animation to the current chain
         *
         * @param transitionTime Time to transition into this animation
         * @param offset         Offset in seconds
         */
        public Builder<T> chain(String anim, int transitionTime, float offset) {
            this.anims.getLast().add(new AnimationHolder(anim, transitionTime, offset * 20));
            return this;
        }

        public AnimationPlayHolder<T> build() {
            return new AnimationPlayHolder<>(this.animation, this.anims.stream().map(ChainedAnimations.ChainListBuilder::build).toList(), this.chance);
        }
    }

    private record ChainedAnimations<T extends Mob & AnimatedEntity>(List<AnimationHolder> chains, Weight weight,
                                                                     Predicate<T> predicate) implements WeightedEntry {

        @Override
        public Weight getWeight() {
            return this.weight();
        }

        private static class ChainListBuilder<T extends Mob & AnimatedEntity> {

            private final ImmutableList.Builder<AnimationHolder> chains = new ImmutableList.Builder<>();
            private final int weight;
            private final Predicate<T> predicate;

            private ChainListBuilder(AnimationHolder action, int weight, Predicate<T> predicate) {
                this.weight = weight;
                this.chains.add(action);
                this.predicate = predicate;
            }

            public void add(AnimationHolder action) {
                this.chains.add(action);
            }

            public ChainedAnimations<T> build() {
                return new ChainedAnimations<>(this.chains.build(), Weight.of(this.weight), this.predicate);
            }
        }
    }

    public record AnimationHolder(String animation, int transitionTime, float offset) {

        public AnimationHolder(String animation) {
            this(animation, AnimationHandler.DEFAULT_TRANSIT_TIME, 0);
        }
    }
}
