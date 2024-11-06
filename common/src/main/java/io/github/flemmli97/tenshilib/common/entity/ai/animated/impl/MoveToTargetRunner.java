package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class MoveToTargetRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final double speed, distance;
    private final boolean accountWidth, requireSight, stopOnReach;

    public MoveToTargetRunner(double speed, double distance) {
        this(speed, distance, true, false, true);
    }

    public MoveToTargetRunner(double speed, double distance, boolean accountWidth, boolean requireSight, boolean stopOnReach) {
        this.speed = speed;
        this.distance = distance;
        this.accountWidth = accountWidth;
        this.requireSight = requireSight;
        this.stopOnReach = stopOnReach;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        double dist = this.accountWidth ? goal.attacker.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 : 0;
        dist += this.distance;
        double distSq = dist * dist;
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        if (goal.distanceToTargetSq < distSq && (!this.requireSight || goal.canSee)) {
            goal.attacker.getLookControl().setLookAt(target, 360, 90);
            if (this.stopOnReach)
                goal.attacker.getNavigation().stop();
            return true;
        }
        goal.moveToTarget(this.speed);
        return false;
    }
}
