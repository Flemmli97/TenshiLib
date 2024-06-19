package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveAwayRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final double minDistSqr, speed;
    private final int dist;

    private boolean start;

    public MoveAwayRunner(double minDist, double speed, int dist) {
        this.minDistSqr = minDist * minDist;
        this.speed = speed;
        this.dist = dist;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        if (!this.start) {
            this.start = true;
            if (goal.distanceToTargetSq < this.minDistSqr) {
                // Move away if too close
                for (int i = 0; i < 10; i++) {
                    Vec3 posAway = DefaultRandomPos.getPosAway(goal.attacker, this.dist, 4, target.position());
                    if (posAway != null) {
                        goal.moveToTargetPosition(posAway.x(), posAway.y(), posAway.z(), this.speed);
                        break;
                    }
                }
            }
        }
        return goal.attacker.getNavigation().isDone();
    }
}
