package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class EvadingRangedRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final double speed, maxDistSqr, minDistSqr;

    private int moveType;

    public EvadingRangedRunner(double maxDist, double minDist, double speed) {
        this.speed = speed;
        this.maxDistSqr = maxDist * maxDist;
        this.minDistSqr = minDist * minDist;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        switch (this.moveType) {
            case 0 -> {
                if (goal.distanceToTargetSq > this.maxDistSqr || !goal.canSee) {
                    // Move towards the target if too far
                    goal.moveToTarget(this.speed);
                    this.moveType = 1;
                } else if (goal.distanceToTargetSq < this.minDistSqr) {
                    // Move away if too close
                    for (int i = 0; i < 10; i++) {
                        Vec3 posAway = DefaultRandomPos.getPosAway(goal.attacker, 7, 4, target.position());
                        if (posAway != null) {
                            goal.moveToTargetPosition(posAway.x(), posAway.y(), posAway.z(), this.speed);
                            break;
                        }
                    }
                    this.moveType = 2;
                }
            }
            case 1 -> {
                if (goal.distanceToTargetSq < this.maxDistSqr) {
                    goal.attacker.getNavigation().stop();
                    return true;
                } else {
                    goal.moveToTarget(this.speed);
                }
            }
            case 2 -> {
                if (goal.distanceToTargetSq > this.maxDistSqr) {
                    goal.attacker.getNavigation().stop();
                    return true;
                }
            }
        }
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        return goal.attacker.getNavigation().isDone();
    }
}
