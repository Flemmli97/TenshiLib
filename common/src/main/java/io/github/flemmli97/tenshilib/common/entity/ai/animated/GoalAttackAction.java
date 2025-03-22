package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import com.google.common.collect.ImmutableList;
import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.AnimationHandler;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.impl.DoNothingRunner;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.impl.WrappedRunner;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * An action instance generator used in animated attack goals
 *
 * @param <T>
 */
public class GoalAttackAction<T extends PathfinderMob & IAnimated> {

    private final AnimatedAction action;
    private Condition<T> condition = (executor, target, previous) -> true;
    private ActionStart.Factory<T> preparation = () -> new WrappedRunner<>(new DoNothingRunner<>());
    private ActionRun.Factory<T> runner = DoNothingRunner::new;
    private IntProvider<T> cooldown = e -> 20;
    private ChainedActions<T> chained;

    public GoalAttackAction(AnimatedAction action) {
        this.action = action;
    }

    /**
     * A condition needed to run this action
     */
    public GoalAttackAction<T> withCondition(Condition<T> condition) {
        this.condition = condition;
        return this;
    }

    /**
     * A preparation handler for this action.
     * After an action is selected it runs this first and when the handler returns true will run the {@link ActionRun}
     */
    public GoalAttackAction<T> prepare(ActionStart.Factory<T> preparation) {
        this.preparation = preparation;
        return this;
    }

    public GoalAttackAction<T> runAction(ActionRun.Factory<T> runner) {
        this.runner = runner;
        return this;
    }

    /**
     * A cooldown for this action.
     * During the cooldown the idle handler will run
     */
    public GoalAttackAction<T> cooldown(IntProvider<T> cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    /**
     * Chain multiple animation attacks together
     * When this action is done the next animation is selected and is executed with a given delay.
     * {@link ChainedActions#chains} Is a list of multiple actions to be run in sequence
     * The list and the sequences inside cannot be empty
     */
    public GoalAttackAction<T> chain(ChainedActions.Builder<T> chained) {
        this.chained = chained.build();
        return this;
    }

    public boolean test(AnimatedAttackGoal<T> goal, LivingEntity target, String previous) {
        return this.condition.test(goal, target, previous);
    }

    public IntProvider<T> getCooldown() {
        return this.cooldown;
    }

    public ChainedActions<T> getChainedAction() {
        return this.chained;
    }

    public ActiveAction<T> createActive() {
        if (this.action == null)
            return null;
        return new ActiveAction<>(this.action, this.preparation.create(), this.runner.create());
    }

    public interface Condition<T extends PathfinderMob & IAnimated> {

        boolean test(AnimatedAttackGoal<T> goal, LivingEntity target, String previous);

    }

    public interface IntProvider<T extends PathfinderMob & IAnimated> {

        int getInt(T entity);

    }

    public record ActiveAction<T extends PathfinderMob & IAnimated>(AnimatedAction anim, ActionStart<T> start,
                                                                    ActionRun<T> runner) {
    }

    public static class ChainedActions<T extends PathfinderMob & IAnimated> {

        private final List<WeightedEntry.Wrapper<ChainList<T>>> chains;
        private final float chance;

        private ChainedActions(List<WeightedEntry.Wrapper<ChainList<T>>> chains, float chance) {
            this.chains = chains;
            this.chance = chance;
        }

        @Nullable
        public List<ChainedAction> get(T mob) {
            if (mob.getRandom().nextFloat() >= this.chance)
                return null;
            List<WeightedEntry.Wrapper<ChainList<T>>> filtered = this.chains.stream().filter(d -> d.getData().predicate().test(mob)).toList();
            return WeightedRandom.getRandomItem(mob.getRandom(), filtered).map(d -> d.getData().chains()).orElse(null);
        }

        public static class Builder<T extends PathfinderMob & IAnimated> {

            private final List<ChainList.ChainListBuilder<T>> anims = new ArrayList<>();
            private float chance = 1;

            private Builder(AnimatedAction anim, int transitionTime, float offset, int weight, Predicate<T> predicate) {
                this.anims.add(new ChainList.ChainListBuilder<>(new ChainedAction(anim, transitionTime, offset * 20),
                        weight, predicate));
            }

            public Builder<T> withChance(float chance) {
                this.chance = chance;
                return this;
            }

            public Builder<T> chain(AnimatedAction anim) {
                return this.chain(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, 0);
            }

            /**
             * Adds an action to the current chain
             *
             * @param offset Offset in seconds
             */
            public Builder<T> chain(AnimatedAction anim, int transitionTime, float offset) {
                this.anims.get(this.anims.size() - 1).add(new ChainedAction(anim, transitionTime, offset * 20));
                return this;
            }

            public Builder<T> or(AnimatedAction anim) {
                return this.or(anim, e -> true);
            }

            public Builder<T> or(AnimatedAction anim, Predicate<T> predicate) {
                return this.or(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, 1, predicate);
            }

            public Builder<T> or(AnimatedAction anim, int transitionTime, float offset, int weight) {
                return this.or(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, 1, e -> true);
            }

            /**
             * Appends a new chain
             *
             * @param offset    Offset in seconds
             * @param predicate Condition required
             */
            public Builder<T> or(AnimatedAction anim, int transitionTime, float offset, int weight, Predicate<T> predicate) {
                this.anims.add(new ChainList.ChainListBuilder<>(new ChainedAction(anim, transitionTime, offset * 20),
                        weight, predicate));
                return this;
            }

            public ChainedActions<T> build() {
                return new ChainedActions<>(this.anims.stream().map(ChainList.ChainListBuilder::build).toList(), this.chance);
            }
        }
    }

    public static <T extends PathfinderMob & IAnimated> ChainedActions.Builder<T> chainBuilder(AnimatedAction anim) {
        return chainBuilder(anim, e -> true);
    }

    public static <T extends PathfinderMob & IAnimated> ChainedActions.Builder<T> chainBuilder(AnimatedAction anim, Predicate<T> delay) {
        return chainBuilder(anim, AnimationHandler.FALLBACK_TRANSIT_TIME, 0, 1, delay);
    }

    public static <T extends PathfinderMob & IAnimated> ChainedActions.Builder<T> chainBuilder(AnimatedAction anim, int transitionTime, float offset, int weight) {
        return chainBuilder(anim, transitionTime, offset, weight, e -> true);
    }

    /**
     * @param anim           The next animation to run
     * @param transitionTime Time to transition into this animation
     * @param offset         Offset of the animation in seconds
     * @param predicate      Condition for this chain
     */
    public static <T extends PathfinderMob & IAnimated> ChainedActions.Builder<T> chainBuilder(AnimatedAction anim, int transitionTime, float offset, int weight, Predicate<T> predicate) {
        return new ChainedActions.Builder<>(anim, transitionTime, offset, weight, predicate);
    }

    private record ChainList<T extends PathfinderMob & IAnimated>(List<ChainedAction> chains, Predicate<T> predicate) {

        private static class ChainListBuilder<T extends PathfinderMob & IAnimated> {

            private final ImmutableList.Builder<ChainedAction> chains = new ImmutableList.Builder<>();
            private final int weight;
            private final Predicate<T> predicate;

            private ChainListBuilder(ChainedAction action, int weight, Predicate<T> predicate) {
                this.weight = weight;
                this.chains.add(action);
                this.predicate = predicate;
            }

            public void add(ChainedAction action) {
                this.chains.add(action);
            }

            public WeightedEntry.Wrapper<ChainList<T>> build() {
                return WeightedEntry.wrap(new ChainList<>(this.chains.build(), this.predicate), this.weight);
            }
        }
    }

    /**
     * A chained AnimatedAction
     *
     * @param anim   The animation to play
     * @param offset Offset tick of the chained animation
     */
    public record ChainedAction(AnimatedAction anim, int transitionTime, float offset) {
    }
}
