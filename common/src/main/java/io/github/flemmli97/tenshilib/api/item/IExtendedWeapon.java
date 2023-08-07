package io.github.flemmli97.tenshilib.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Items with modified attack range
 */
public interface IExtendedWeapon {

    default float getRange(LivingEntity entity, ItemStack stack) {
        return 3;
    }
}
