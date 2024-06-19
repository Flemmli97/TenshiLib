package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class JumpEvadeAction<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final double minDistSqr, power, powerSide;
    private final float chance, sideChance;
    private final ActionRun<T> other;

    private boolean start;

    public JumpEvadeAction(double minDist, double power, double powerSide, float chance, float sideChance, ActionRun<T> other) {
        this.minDistSqr = minDist * minDist;
        this.power = power;
        this.powerSide = powerSide;
        this.chance = chance;
        this.sideChance = sideChance;
        this.other = other;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        if (!this.start || goal.attacker.getRandom().nextFloat() < this.chance) {
            this.start = true;
            if (goal.distanceToTargetSq < this.minDistSqr) {
                Vec3 dir = target.position().subtract(goal.attacker.position());
                dir = new Vec3(dir.x(), 0, dir.z()).normalize().scale(this.power);
                goal.attacker.setDeltaMovement(-dir.x(), 0.2, -dir.z());
            } else if (goal.attacker.getRandom().nextFloat() < this.sideChance) {
                Vec3 dir = target.position().subtract(goal.attacker.position());
                dir = new Vec3(dir.x(), 0, dir.z()).normalize().scale(this.powerSide);
                dir = MathUtils.rotate(new Vec3(0, 1, 0), dir, goal.attacker.getRandom().nextBoolean() ? 90 : -90);
                goal.attacker.setDeltaMovement(-dir.x(), 0.2, -dir.z());
            }
        }
        return this.other.run(goal, target, anim);
    }
}
