package io.github.flemmli97.tenshilib.api.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Items with modified attack range
 */
public interface IExtendedWeapon {

    default float getRange(LivingEntity entity, ItemStack stack) {
        return 3;
    }

    default boolean resetAttackStrength(LivingEntity entity, ItemStack stack) {
        return true;
    }

    default boolean swingWeapon(LivingEntity entity, ItemStack stack) {
        return true;
    }

    default boolean onHit(LivingEntity entity, ItemStack stack, Entity target) {
        return true;
    }
}
