package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

/**
 * Action instance that will run during cooldowns
 *
 * @param <T>
 */
public class IdleAction<T extends PathfinderMob & IAnimated> {

    public final ActionRun.Factory<T> runner;
    private Condition<T> condition = (executor, target) -> true;
    private GoalAttackAction.IntProvider<T> duration = e -> 20;

    public IdleAction(ActionRun.Factory<T> runner) {
        this.runner = runner;
    }

    public IdleAction<T> withCondition(Condition<T> condition) {
        this.condition = condition;
        return this;
    }

    public IdleAction<T> duration(GoalAttackAction.IntProvider<T> duration) {
        this.duration = duration;
        return this;
    }

    public boolean test(AnimatedAttackGoal<T> goal, LivingEntity target) {
        return this.condition.test(goal, target);
    }

    public GoalAttackAction.IntProvider<T> getDuration() {
        return this.duration;
    }

    public interface Condition<T extends PathfinderMob & IAnimated> {

        boolean test(AnimatedAttackGoal<T> goal, LivingEntity target);

    }
}
