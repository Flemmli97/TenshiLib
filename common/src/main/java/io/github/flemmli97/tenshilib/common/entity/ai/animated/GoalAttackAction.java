package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.impl.DoNothingRunner;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.impl.WrappedRunner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

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

    public boolean test(AnimatedAttackGoal<T> goal, LivingEntity target, String previous) {
        return this.condition.test(goal, target, previous);
    }

    public IntProvider<T> getCooldown() {
        return this.cooldown;
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
}
