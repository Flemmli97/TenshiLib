package io.github.flemmli97.tenshilib.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Implementing this will make items dual weapons. Dual weapons prevent offhand use and are also rendered in both hands
 */
public interface IDualWeapon {

    /**
     * Change the item being rendered in the offhand
     */
    default ItemStack offHandStack(LivingEntity entity) {
        return entity.getMainHandItem();
    }

    default boolean disableOffhand() {
        return true;
    }
}
