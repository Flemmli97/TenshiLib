package io.github.flemmli97.tenshilib.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Items with modified attack range
 */
public interface IExtendedWeapon {

    /**
     * Use ItemStack sensitive version
     */
    @Deprecated(forRemoval = true, since = "1.6.15")
    default float getRange() {
        return 3;
    }

    default float getRange(LivingEntity entity, ItemStack stack) {
        return this.getRange();
    }
}
