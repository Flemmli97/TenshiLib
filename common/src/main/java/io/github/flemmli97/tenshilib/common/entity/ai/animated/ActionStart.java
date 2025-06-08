package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.common.entity.AnimatedEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public interface ActionStart<T extends PathfinderMob & AnimatedEntity> {

    /**
     * The maximum time it gets to prepare. If during this #start doesn't return true then this task gets aborted
     */
    GoalAttackAction.IntProvider<T> timeout();

    /**
     * Start the runner.
     *
     * @return true to indicate a success and stop the preparation
     */
    boolean start(AnimatedAttackGoal<T> goal, LivingEntity target);

    interface Factory<T extends PathfinderMob & AnimatedEntity> {

        ActionStart<T> create();

    }
}
