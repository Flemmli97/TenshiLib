package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.AoeAttackEntity;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;

public class MoveToTargetAttackRunner<T extends PathfinderMob & IAnimated & AoeAttackEntity> implements ActionRun<T> {

    private final double speed;
    private final boolean needsLoS, stopOnReach;

    public MoveToTargetAttackRunner(double speed) {
        this(speed, true, true);
    }

    public MoveToTargetAttackRunner(double speed, boolean needsLoS, boolean stopOnReach) {
        this.speed = speed;
        this.needsLoS = needsLoS;
        this.stopOnReach = stopOnReach;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        if (anim == null)
            return false;
        AABB aabb = goal.attacker.prepareAttackBox(anim, target, -0.15, true);
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        if (aabb.intersects(target.getBoundingBox()) && (!this.needsLoS || goal.canSee)) {
            goal.attacker.getLookControl().setLookAt(target, 360, 90);
            if (this.stopOnReach)
                goal.attacker.getNavigation().stop();
            return true;
        }
        goal.moveToTarget(this.speed);
        return false;
    }
}
