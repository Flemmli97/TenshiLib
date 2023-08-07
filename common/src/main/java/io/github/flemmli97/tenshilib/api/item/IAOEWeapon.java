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
}
