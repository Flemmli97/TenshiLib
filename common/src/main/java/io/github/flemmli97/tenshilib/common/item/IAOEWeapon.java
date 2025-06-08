package io.github.flemmli97.tenshilib.common.item;

import io.github.flemmli97.tenshilib.common.utils.OrientedBoundingBox;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Items with modified attack range and aoe.
 */
public interface IAOEWeapon {

    static OrientedBoundingBox createOBB(LivingEntity entity, ItemStack stack, double length, double width) {
        AABB aabb = new AABB(-width * 0.5, 0, 0, width * 0.5, 1, length);
        float yRot = entity.getYRot();
        float xRot = -entity.getXRot();
        return new OrientedBoundingBox(aabb
                .move(0, -aabb.getYsize() * 0.5, 0), yRot, xRot, entity.getEyePosition());
    }

    default float getRange(LivingEntity entity, ItemStack stack) {
        return 3;
    }

    default float getWidth(LivingEntity entity, ItemStack stack) {
        return 0.5f;
    }

    default AABB attackBB(LivingEntity entity, ItemStack stack) {
        double width = this.getWidth(entity, stack);
        if (width == 0)
            return null;
        double length = this.getRange(entity, stack);
        return new AABB(-width * 0.5, 0, 0, width * 0.5, 1, length);
    }

    default OrientedBoundingBox attackOBB(LivingEntity entity, ItemStack stack, boolean debug) {
        AABB aabb = this.attackBB(entity, stack);
        if (aabb == null)
            return null;
        float yRot = entity.getYRot();
        float xRot = -entity.getXRot();
        return new OrientedBoundingBox(aabb
                .move(0, -aabb.getYsize() * 0.5, 0), yRot, xRot, entity.getEyePosition());
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
     * If true does 2 things
     * 1. When clicking on a block will attack using that item
     * 2. Disables mining blocks with this item
     */
    default boolean disableBlockAttack(LivingEntity entity, ItemStack stack) {
        return true;
    }
}
