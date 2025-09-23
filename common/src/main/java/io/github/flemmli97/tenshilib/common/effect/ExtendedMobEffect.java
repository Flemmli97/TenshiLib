package io.github.flemmli97.tenshilib.common.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Extension to {@link net.minecraft.world.effect.MobEffect}
 */
public interface ExtendedMobEffect {

    default boolean shouldSync() {
        return true;
    }

    default void onEffectAdded(LivingEntity entity, MobEffectInstance instance) {

    }

    default void onEffectUpdated(LivingEntity entity, MobEffectInstance instance) {

    }

    default void onEffectRemoved(LivingEntity entity, MobEffectInstance instance) {

    }
}
