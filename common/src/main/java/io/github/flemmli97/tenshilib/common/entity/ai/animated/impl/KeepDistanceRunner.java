package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class KeepDistanceRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final double speed, maxDistSqr, minDistSqr;
    private final int dist;
    private final boolean needsLoS;

    private int moveType;
    private ActionUtils.PathDistance pathDist;

    public KeepDistanceRunner(double minDist, double maxDist) {
        this(minDist, maxDist, 1, true);
    }

    public KeepDistanceRunner(double minDist, double maxDist, double speed) {
        this(minDist, maxDist, speed, true);
    }

    public KeepDistanceRunner(double minDist, double maxDist, double speed, boolean needsLoS) {
        this.speed = speed;
        this.maxDistSqr = maxDist * maxDist;
        this.minDistSqr = minDist * minDist;
        this.dist = Mth.ceil(maxDist - minDist);
        this.needsLoS = needsLoS;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        switch (this.moveType) {
            case 0 -> {
                if (goal.distanceToTargetSq > this.maxDistSqr || !this.canSee(goal)) {
                    // Move towards the target if too far
                    goal.moveToTarget(this.speed);
                    this.moveType = 1;
                } else if (goal.distanceToTargetSq < this.minDistSqr) {
                    // Move away if too close
                    for (int i = 0; i < 10; i++) {
                        Vec3 posAway = DefaultRandomPos.getPosAway(goal.attacker, this.dist, 4, target.position());
                        if (posAway != null) {
                            goal.moveToTargetPosition(posAway.x(), posAway.y(), posAway.z(), this.speed);
                            break;
                        }
                    }
                    this.moveType = 2;
                }
            }
            case 1 -> {
                if (goal.distanceToTargetSq < this.maxDistSqr && this.canSee(goal)) {
                    goal.attacker.getNavigation().stop();
                    return true;
                } else {
                    goal.moveToTarget(this.speed);
                }
            }
            case 2 -> {
                if (goal.attacker.tickCount % 3 == 0) {
                    // Check if entity is getting close. If not retry
                    ActionUtils.PathDistance lastCheck = this.pathDist;
                    this.pathDist = ActionUtils.distanceToNavTargetSqr(goal.attacker);
                    if (lastCheck != null && this.pathDist != null) {
                        if (lastCheck.index() == this.pathDist.index() && lastCheck.dist() + 2 <= this.pathDist.dist()) {
                            this.moveType = 0;
                            return false;
                        }
                    }
                }
                // Reset if out of sight
                if (!this.canSee(goal)) {
                    this.moveType = 0;
                    return false;
                }
                if (goal.distanceToTargetSq > this.maxDistSqr) {
                    goal.attacker.getNavigation().stop();
                    return true;
                }
            }
        }
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        return goal.attacker.getNavigation().isDone() && this.canSee(goal);
    }

    private boolean canSee(AnimatedAttackGoal<T> goal) {
        return !this.needsLoS || goal.canSee;
    }
}
