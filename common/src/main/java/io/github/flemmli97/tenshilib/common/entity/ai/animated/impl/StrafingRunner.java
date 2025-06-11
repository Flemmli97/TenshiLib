package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class StrafingRunner<T extends PathfinderMob & AnimatedEntity> implements ActionRun<T> {

    private final float radiusSq, minRadiusSq, speed, directionSwitchChance;

    private boolean start, clockWise;
    private int strafingTime;
    private int seeTime = 10;
    private boolean strafingBackwards;

    public StrafingRunner(float radius, float speed) {
        this(radius, speed, 0.3f);
    }

    public StrafingRunner(float radius, float speed, float directionSwitchChance) {
        this(radius, radius * 0.5f, speed, directionSwitchChance);
    }

    public StrafingRunner(float radius, float minRadius, float speed, float directionSwitchChance) {
        this.radiusSq = radius * radius;
        this.minRadiusSq = minRadius * minRadius;
        this.speed = speed;
        this.directionSwitchChance = directionSwitchChance;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimationState anim) {
        if (!this.start) {
            this.start = true;
            this.clockWise = goal.attacker.getRandom().nextBoolean();
        }
        boolean saw = this.seeTime > 0;
        if (goal.canSee != saw) {
            this.seeTime = 0;
        }

        if (goal.canSee) {
            ++this.seeTime;
        } else {
            --this.seeTime;
        }
        if (goal.distanceToTargetSq <= this.radiusSq * this.radiusSq && this.seeTime >= 20) {
            goal.attacker.getNavigation().stop();
            ++this.strafingTime;
        } else {
            goal.moveToTarget(this.speed);
            this.strafingTime = -1;
        }
        if (this.strafingTime >= 20) {
            if (goal.attacker.getRandom().nextFloat() < this.directionSwitchChance) {
                this.clockWise = !this.clockWise;
            }
            this.strafingTime = 0;
        }
        if (this.strafingTime > -1) {
            if (goal.distanceToTargetSq > this.radiusSq * 0.75) {
                this.strafingBackwards = false;
            } else if (goal.distanceToTargetSq < this.minRadiusSq) {
                this.strafingBackwards = true;
            }
            goal.attacker.getMoveControl().strafe(this.strafingBackwards ? -0.5f : 0.5f, this.clockWise ? this.speed : -this.speed);
        }
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        return false;
    }
}
