package io.github.flemmli97.tenshilib.common.entity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityUtils {

    @Nullable
    public static <T extends Entity> T findFromUUID(Class<T> clss, Level level, UUID uuid) {
        return findFromUUID(clss, level, uuid, t -> true);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Entity> T findFromUUID(Class<T> clss, Level level, UUID uuid, Predicate<T> pred) {
        if (level instanceof ServerLevel) {
            Entity e = ((ServerLevel) level).getEntity(uuid);
            if (e != null && clss.isAssignableFrom(e.getClass()) && pred.test((T) e))
                return (T) e;
        } else {
            for (Entity e : ((ClientLevel) level).entitiesForRendering()) {
                if (e.getUUID().equals(uuid) && clss.isAssignableFrom(e.getClass()) && pred.test((T) e))
                    return (T) e;
            }
        }
        return null;
    }

    public static ItemStack findItem(Player player, Predicate<ItemStack> pred, boolean searchArmor, boolean offHand) {
        for (ItemStack stack : player.getInventory().items)
            if (pred.test(stack))
                return stack;
        if (searchArmor)
            for (ItemStack stack : player.getInventory().armor)
                if (pred.test(stack))
                    return stack;
        if (offHand)
            for (ItemStack stack : player.getInventory().offhand)
                if (pred.test(stack))
                    return stack;
        return ItemStack.EMPTY;
    }

    public static Vec3 getStraightProjectileTarget(Vec3 from, Entity target) {
        AABB aabb = target.getBoundingBox();
        return getStraightProjectileTarget(from, target.position(), aabb.minY + target.getBbHeight() * 0.15, aabb.maxY - target.getBbHeight() * 0.15);
    }

    public static Vec3 getStraightProjectileTarget(Vec3 from, Vec3 target, double minY, double maxY) {
        return new Vec3(target.x(), Mth.clamp(from.y(), minY, maxY), target.z());
    }

    /**
     * Returns a random collision free position around the given blockpos.
     * Null if it was not possible
     */
    @Nullable
    public static BlockPos randomPosAround(Level level, Entity entity, BlockPos pos, int range, boolean grounded, Random rand) {
        int randX = pos.getX() + rand.nextInt(2 * range) - range;
        int randY = pos.getY() + rand.nextInt(2 * range) - range;
        int randZ = pos.getZ() + rand.nextInt(2 * range) - range;
        if (!grounded) {
            BlockPos pos1 = new BlockPos(randX, randY, randZ);
            while (Math.abs(randY - pos1.getY()) < range && !level.noCollision(entity.getBoundingBox().move(pos1))) {
                pos1 = pos1.above();
            }
            if (!level.noCollision(entity.getBoundingBox().move(pos1)))
                return null;
            return pos1;
        }
        int y = pos.getY() - range;
        BlockPos pos1 = new BlockPos(randX, y, randZ);
        while (pos1.getY() - y < range && (!level.getBlockState(pos1.below()).entityCanStandOnFace(level, pos1.below(), entity, Direction.UP)
                || !level.noCollision(entity.getBoundingBox().move(pos1)))) {
            pos1 = pos1.above();
        }
        if (!level.noCollision(entity.getBoundingBox().move(pos1)))
            return null;
        return pos1;
    }
}
