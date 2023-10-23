package io.github.flemmli97.tenshilib.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Items with modified attack range and aoe.
 */
public interface IAOEWeapon {

    default float getRange(LivingEntity entity, ItemStack stack) {
        return 3;
    }

    /**
     * @return FOV of the weapons aoe area in degree. E.g. getFOV()==10 would mean all entities at the crosshair position +- 10 degrees left and right. Max 180
     */
    default float getFOV(LivingEntity entity, ItemStack stack) {
        return 0;
    }

    default boolean doSweepingAttack() {
        return true;
    }

    default boolean resetAttackStrength(LivingEntity entity, ItemStack stack) {
        return true;
    }

    /**
     * @return If false disables swing animation
     */
    default boolean swingWeapon(LivingEntity entity, ItemStack stack) {
        return true;
    }

    /**
     * @return If false will disable the normal attack that runs when left clicking with this item
     */
    default boolean onServerSwing(LivingEntity entity, ItemStack stack) {
        return true;
    }

    /**
     * If false does 2 things
     * 1. When clicking on a block will attack using that item
     * 2. Disables mining blocks with this item
     */
    default boolean allowBlockAttack(LivingEntity entity, ItemStack stack) {
        return false;
    }
}
