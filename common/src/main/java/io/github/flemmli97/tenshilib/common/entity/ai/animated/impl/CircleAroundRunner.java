package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class CircleAroundRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final float radius;
    private final float speed;

    private boolean start, clockWise;

    public CircleAroundRunner(float radius, float speed) {
        this.radius = radius;
        this.speed = speed;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        if (!this.start) {
            this.start = true;
            this.clockWise = goal.attacker.getRandom().nextBoolean();
        }
        goal.circleAroundTargetFacing(this.radius, this.clockWise, this.speed);
        return false;
    }
}
