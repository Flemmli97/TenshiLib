package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.AnimationHandler;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.impl.DoNothingRunner;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.impl.WrappedRunner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

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
     * {@link ChainedActions#anims} Is a list of multiple actions to be run in sequence
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

    public record ChainedActions<T extends PathfinderMob & IAnimated>(List<List<ChainedAction<T>>> anims,
                                                                      Predicate<T> check) {

        public static class Builder<T extends PathfinderMob & IAnimated> {

            private final List<List<ChainedAction<T>>> anims = new ArrayList<>();
            private Predicate<T> check = e -> true;

            private Builder(AnimatedAction anim, int transitionTime, float offset, IntProvider<T> delay) {
                List<ChainedAction<T>> list = new ArrayList<>();
                list.add(new ChainedAction<>(anim, transitionTime, offset, delay));
                this.anims.add(list);
            }

            public Builder<T> chain(AnimatedAction anim) {
                return this.chain(anim, e -> 0);
            }

            public Builder<T> chain(AnimatedAction anim, IntProvider<T> delay) {
                return this.chain(anim, AnimationHandler.DEFAULT_TRANSIT_TIME, 0, delay);
            }

            /**
             * Adds an action to the current chain
             */
            public Builder<T> chain(AnimatedAction anim, int transitionTime, float offset, IntProvider<T> delay) {
                this.anims.get(this.anims.size() - 1).add(new ChainedAction<>(anim, transitionTime, offset, delay));
                return this;
            }

            public Builder<T> or(AnimatedAction anim) {
                return this.or(anim, e -> 0);
            }

            /**
             * Appends a new chain
             */
            public Builder<T> or(AnimatedAction anim, IntProvider<T> delay) {
                return this.or(anim, AnimationHandler.DEFAULT_TRANSIT_TIME, 0, delay);
            }

            /**
             * Appends a new chain
             */
            public Builder<T> or(AnimatedAction anim, int transitionTime, float offset, IntProvider<T> delay) {
                List<ChainedAction<T>> list = new ArrayList<>();
                list.add(new ChainedAction<>(anim, transitionTime, offset, delay));
                this.anims.add(list);
                return this;
            }

            public Builder<T> withPredicate(Predicate<T> check) {
                this.check = check;
                return this;
            }

            public ChainedActions<T> build() {
                return new ChainedActions<>(List.copyOf(this.anims), this.check);
            }
        }
    }

    public static <T extends PathfinderMob & IAnimated> ChainedActions.Builder<T> chainBuilder(AnimatedAction anim) {
        return chainBuilder(anim, e -> 0);
    }

    public static <T extends PathfinderMob & IAnimated> ChainedActions.Builder<T> chainBuilder(AnimatedAction anim, IntProvider<T> delay) {
        return chainBuilder(anim, AnimationHandler.DEFAULT_TRANSIT_TIME, 0, delay);
    }

    public static <T extends PathfinderMob & IAnimated> ChainedActions.Builder<T> chainBuilder(AnimatedAction anim, int transitionTime, float offset, IntProvider<T> delay) {
        return new ChainedActions.Builder<>(anim, transitionTime, offset, delay);
    }

    /**
     * A chained AnimatedAction
     *
     * @param anim   The animation to play
     * @param offset Offset tick of the chained animation
     * @param delay  A delay after which this animation will be played
     */
    public record ChainedAction<T extends PathfinderMob & IAnimated>(AnimatedAction anim, int transitionTime,
                                                                     float offset,
                                                                     IntProvider<T> delay) {
    }
}
