package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionStart;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.GoalAttackAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

/**
 * A wrapper around a runner that will return the runners result
 *
 * @param <T>
 */
public class WrappedRunner<T extends PathfinderMob & IAnimated> implements ActionStart<T> {

    private final GoalAttackAction.IntProvider<T> max;
    private final ActionRun<T> runner;

    public WrappedRunner(ActionRun<T> runner) {
        this(e -> 200, runner);
    }

    public WrappedRunner(GoalAttackAction.IntProvider<T> timout, ActionRun<T> runner) {
        this.max = timout;
        this.runner = runner;
    }

    @Override
    public GoalAttackAction.IntProvider<T> timeout() {
        return this.max;
    }

    @Override
    public boolean start(AnimatedAttackGoal<T> goal, LivingEntity target) {
        return this.runner.run(goal, target, goal.current != null ? goal.current.anim() : null);
    }
}
