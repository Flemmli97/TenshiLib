package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.common.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.AnimatedEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;

public interface ActionRun<T extends PathfinderMob & AnimatedEntity> {

    boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, @Nullable AnimatedAction anim);

    interface Factory<T extends PathfinderMob & AnimatedEntity> {

        ActionRun<T> create();

    }
}
