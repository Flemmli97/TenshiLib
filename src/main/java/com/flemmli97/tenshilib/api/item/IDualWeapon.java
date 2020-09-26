package com.flemmli97.tenshilib.api.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Implementing this will make items dual weapons. Dual weapons prevent offhand use and are also rendered in both hands
 */
public interface IDualWeapon {

    /**
     * Change the item being rendered in the offhand
     */
    default ItemStack offHandStack(LivingEntity entity) {
        return entity.getHeldItemMainhand();
    }

    default boolean disableOffhand() {
        return true;
    }
}
