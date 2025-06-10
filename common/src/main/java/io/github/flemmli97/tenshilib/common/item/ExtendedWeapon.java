package io.github.flemmli97.tenshilib.common.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

// TODO
public interface ExtendedWeapon {

    default boolean shouldAttack(HitResult hitResult) {
        return hitResult.getType() == HitResult.Type.ENTITY;
    }

    default boolean resetAttackStrength(LivingEntity entity, ItemStack stack) {
        return true;
    }

    default boolean shouldSwingWeapon(LivingEntity entity, ItemStack stack) {
        return true;
    }

    default boolean onTryAttackServer(LivingEntity entity, ItemStack stack) {
        return true;
    }

    default boolean onHit(LivingEntity entity, ItemStack stack, Entity target) {
        return true;
    }
}
