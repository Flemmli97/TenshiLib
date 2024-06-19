package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

/**
 * Assumes the entity floats
 */
public class StayWithinHeightAction<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final Double min, max;
    private final ActionRun<T> other;

    public StayWithinHeightAction(Double min, ActionRun<T> other) {
        this(min, null, other);
    }

    public StayWithinHeightAction(Double min, Double max, ActionRun<T> other) {
        this.min = min;
        this.max = max;
        this.other = other;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        if (this.min != null && goal.attacker.getY() < (target.getY() - this.min))
            goal.attacker.setDeltaMovement(target.getDeltaMovement().add(0, 0.03, 0));
        else if (this.max != null && goal.attacker.getY() > (target.getY() + this.max))
            goal.attacker.setDeltaMovement(target.getDeltaMovement().add(0, -0.03, 0));
        return this.other.run(goal, target, anim);
    }
}
