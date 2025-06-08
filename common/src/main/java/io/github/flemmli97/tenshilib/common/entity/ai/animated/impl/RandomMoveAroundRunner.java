package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.common.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class RandomMoveAroundRunner<T extends PathfinderMob & AnimatedEntity> implements ActionRun<T> {

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
            goal.attacker.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (goal.distanceToTargetSq <= this.maxDistSqr) {
                if (goal.attacker.getNavigation().isDone()) {
                    for (int i = 0; i < 10; i++) {
                        Vec3 rand = DefaultRandomPos.getPos(goal.attacker, this.distance, 4);
                        if (rand != null && rand.distanceToSqr(target.position()) < this.maxDistSqr) {
                            Path path = goal.attacker.getNavigation().createPath(rand.x, rand.y, rand.z, 0);
                            if (path != null) {
                                goal.attacker.getNavigation().moveTo(path, 1);
                                break;
                            }
                        }
                    }
                }
            } else {
                goal.moveToTarget(1);
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
