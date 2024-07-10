package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class StrafingRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final float radius, speed, directionSwitchChance;

    private boolean start, clockWise;
    private int strafingTime;
    private int seeTime;

    public StrafingRunner(float radius, float speed) {
        this(radius, speed, 0.3f);
    }

    public StrafingRunner(float radius, float speed, float directionSwitchChance) {
        this.radius = radius;
        this.speed = speed;
        this.directionSwitchChance = directionSwitchChance;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
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
        if (goal.distanceToTargetSq <= this.radius * this.radius && this.seeTime >= 20) {
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
            goal.circleAroundTargetFacing(this.radius, this.clockWise, this.speed);
        }
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        return false;
    }
}
