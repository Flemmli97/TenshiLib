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
    private ActionUtils.PathDistance pathDist;

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
        if (goal.attacker.tickCount % 3 == 0) {
            // Check if entity is getting close. If not retry
            ActionUtils.PathDistance lastCheck = this.pathDist;
            this.pathDist = ActionUtils.distanceToNavTargetSqr(goal.attacker);
            if (lastCheck != null && this.pathDist != null) {
                if (lastCheck.index() == this.pathDist.index() && lastCheck.dist() + 2 <= this.pathDist.dist()) {
                    this.start = false;
                    return false;
                }
            }
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
