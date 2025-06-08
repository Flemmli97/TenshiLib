package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.common.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveAwayRunner<T extends PathfinderMob & AnimatedEntity> implements ActionRun<T> {

    private final double minDistSqr, speed;
    private final int dist;
    private final boolean needsLoS;

    private boolean start;
    private ActionUtils.PathDistance pathDist;

    public MoveAwayRunner(double minDist, double speed, int dist) {
        this(minDist, speed, dist, false);
    }

    public MoveAwayRunner(double minDist, double speed, int dist, boolean needsLoS) {
        this.minDistSqr = minDist * minDist;
        this.speed = speed;
        this.dist = dist;
        this.needsLoS = needsLoS;
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
