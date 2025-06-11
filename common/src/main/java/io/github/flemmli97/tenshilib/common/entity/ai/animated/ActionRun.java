package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;

public interface ActionRun<T extends PathfinderMob & AnimatedEntity> {

    boolean run(AnimatedAttackGoal<T> goal, LivingEntity target, @Nullable AnimationState anim);

    interface Factory<T extends PathfinderMob & AnimatedEntity> {

        ActionRun<T> create();

    }
}
