package io.github.flemmli97.tenshilib.common.entity.ai.animated.impl;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.ActionRun;
import io.github.flemmli97.tenshilib.common.entity.ai.animated.AnimatedAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class DoNothingRunner<T extends PathfinderMob & IAnimated> implements ActionRun<T> {

    private final boolean doneImmediately;

    public DoNothingRunner() {
        this(false);
    }

    public DoNothingRunner(boolean doneImmediately) {
        this.doneImmediately = doneImmediately;
    }

    @Override
    public boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, AnimatedAction anim) {
        goal.attacker.getNavigation().stop();
        goal.attacker.lookAt(target, 30.0F, 30.0F);
        return this.doneImmediately;
    }
}
