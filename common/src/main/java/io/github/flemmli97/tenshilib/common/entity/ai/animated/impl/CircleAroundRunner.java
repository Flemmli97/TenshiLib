package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class CircleAroundRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final float radius, speed, clockWiseChance;

    private boolean start, clockWise;
    private Vec3 startPos;

    public CircleAroundRunner(float radius, float speed, float clockWiseChance) {
        this.radius = radius;
        this.speed = speed;
        this.clockWiseChance = clockWiseChance;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        if (!this.start) {
            this.start = true;
            this.clockWise = goal.attacker.getRandom().nextFloat() < this.clockWiseChance;
            this.startPos = goal.attacker.position();
        }
        goal.circleAround(this.startPos.x, this.startPos.z, this.radius, this.clockWise, this.speed);
        return false;
    }
}
