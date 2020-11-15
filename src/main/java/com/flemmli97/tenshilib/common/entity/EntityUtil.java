package com.flemmli97.tenshilib.common.entity;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityUtil {

    @Nullable
    public static <T extends Entity> T findFromUUID(Class<T> clss, World world, UUID uuid) {
        return findFromUUID(clss, world, uuid, t -> true);
    }

    //TODO: Test on server
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Entity> T findFromUUID(Class<T> clss, World world, UUID uuid, Predicate<T> pred) {
        if (world instanceof ServerWorld) {
            Entity e = ((ServerWorld) world).getEntityByUuid(uuid);
            if (e != null && clss.isAssignableFrom(e.getClass()) && pred.test((T) e))
                return (T) e;
        } else {
            for (Entity e : ((ClientWorld) world).getAllEntities()) {
                if (e.getUniqueID().equals(uuid) && clss.isAssignableFrom(e.getClass()) && pred.test((T) e))
                    return (T) e;
            }
        }
        return null;
    }

    public static ItemStack findItem(PlayerEntity player, Predicate<ItemStack> pred, boolean searchArmor, boolean offHand) {
        for (ItemStack stack : player.inventory.mainInventory)
            if (pred.test(stack))
                return stack;
        if (searchArmor)
            for (ItemStack stack : player.inventory.armorInventory)
                if (pred.test(stack))
                    return stack;
        if (offHand)
            for (ItemStack stack : player.inventory.offHandInventory)
                if (pred.test(stack))
                    return stack;
        return ItemStack.EMPTY;
    }
}
