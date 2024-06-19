package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionStart;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.GoalAttackAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

/**
 * A wrapper around a runner that will run either till the runner returns true or for the given duration.
 *
 * @param <T>
 */
public class TimedWrappedRunner<T extends PathfinderMob & IAnimated> implements ActionStart<T> {

    private final GoalAttackAction.IntProvider<T> timeout, duration;
    private final ActionRun<T> runner;

    private int timer = -1;

    public TimedWrappedRunner(ActionRun<T> runner, GoalAttackAction.IntProvider<T> duration) {
        this(runner, duration, e -> 200);
    }

    public TimedWrappedRunner(ActionRun<T> runner, GoalAttackAction.IntProvider<T> duration, GoalAttackAction.IntProvider<T> timeout) {
        this.timeout = timeout;
        this.runner = runner;
        this.duration = duration;
    }

    @Override
    public GoalAttackAction.IntProvider<T> timeout() {
        return this.timeout;
    }

    @Override
    public boolean start(AnimatedAttackGoal<T> goal, LivingEntity target) {
        if (this.timer == -1) {
            this.timer = this.duration.getInt(goal.attacker);
        }
        return --this.timer == 0 || this.runner.run(goal, target, goal.current != null ? goal.current.anim() : null);
    }
}
