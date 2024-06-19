package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class MoveToTargetRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final double speed, distanceSqr;
    private final boolean accountWidth, requireSight;

    public MoveToTargetRunner(double speed, double distance) {
        this(speed, distance, true, false);
    }

    public MoveToTargetRunner(double speed, double distance, boolean accountWidth, boolean requireSight) {
        this.speed = speed;
        this.distanceSqr = distance * distance;
        this.accountWidth = accountWidth;
        this.requireSight = requireSight;
    }


    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        goal.moveToTarget(this.speed);
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        double dist = this.accountWidth ? goal.attacker.getBbWidth() * goal.attacker.getBbWidth() : 0;
        dist += this.distanceSqr;
        if (goal.distanceToTargetSq < dist && (!this.requireSight || goal.canSee)) {
            goal.attacker.getLookControl().setLookAt(target, 360, 90);
            return true;
        }
        return false;
    }
}
