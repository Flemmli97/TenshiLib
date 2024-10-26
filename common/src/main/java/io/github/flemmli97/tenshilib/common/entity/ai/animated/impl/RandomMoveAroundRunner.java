package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class RandomMoveAroundRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final double maxDistSqr;
    private final int distance;
    private final boolean needsLoS;

    private boolean start;

    public RandomMoveAroundRunner(double maxDist, int distance) {
        this(maxDist, distance, true);
    }

    public RandomMoveAroundRunner(double maxDist, int distance, boolean needsLoS) {
        this.maxDistSqr = maxDist * maxDist;
        this.distance = distance;
        this.needsLoS = needsLoS;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        if (!this.start) {
            this.start = true;
            goal.moveRandomlyAround(this.maxDistSqr, this.distance);
        }
        boolean done = goal.attacker.getNavigation().isDone();
        if (done && !this.canSee(goal)) {
            this.start = false;
            return false;
        }
        return done;
    }

    private boolean canSee(AnimatedAttackGoal<T> goal) {
        return !this.needsLoS || goal.canSee;
    }
}
