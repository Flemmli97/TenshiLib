package com.flemmli97.tenshilib.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;


public class RayTraceUtils {

    /**
     * Gets a list of entities in a certain fov around the player
     *
     * @param reach Radius around the entity
     * @param aoe   FOV in degrees. 0 means vanilla raytracing. use 1 to have it like vanilla but get multiple entities.
     */
    public static List<Entity> getEntities(LivingEntity entity, float reach, float aoe) {
        return getEntities(entity, reach, aoe, null);
    }

    public static List<Entity> getEntities(LivingEntity entity, float reach, float aoe, Predicate<Entity> pred) {
        return getEntitiesIn(entity, entity.getPositionVec().add(0, entity.getEyeHeight(), 0), entity.getLook(1), reach,
                aoe, pred);
    }

    public static List<Entity> getEntitiesIgnorePitch(LivingEntity entity, float reach, float aoe, Predicate<Entity> pred) {
        return getEntitiesIn(entity, entity.getPositionVec().add(0, entity.getEyeHeight(), 0), Vector3d.fromPitchYaw(0, entity.getYaw(1)), reach,
                aoe, pred);
    }

    public static List<Entity> getEntitiesIn(LivingEntity entity, Vector3d pos, Vector3d look, float reach,
                                             float aoe, Predicate<Entity> pred) {
        CircleSector circ = new CircleSector(pos, look, reach, aoe, entity);
        return entity.world.getEntitiesInAABBexcluding(entity, entity.getBoundingBox().grow(reach),
                t -> t != entity && (pred == null || pred.test(t)) && !t.isOnSameTeam(entity) && t.canBeCollidedWith()
                        && circ.intersects(t.world, t.getBoundingBox()));
    }

    public static EntityRayTraceResult calculateEntityFromLook(LivingEntity entity, float reach) {
        return calculateEntityFromLook(entity, entity.getEyePosition(1), entity.getLook(1), reach, null);
    }

    public static EntityRayTraceResult calculateEntityFromLook(LivingEntity entity, Vector3d pos, Vector3d dir, float reach,
                                                               @Nullable Predicate<Entity> pred) {
        Vector3d end = pos.add(dir.scale(reach));
        return ProjectileHelper.rayTraceEntities(entity.world, entity, pos, pos.add(dir.scale(reach)), entity.getBoundingBox().expand(end).grow(1), (t) -> EntityPredicates.NOT_SPECTATING.test(t) && t.canBeCollidedWith()
                && (pred == null || pred.test(t)));
        /*RayTraceResult blocks = entity.world.rayTraceBlocks(new RayTraceContext(pos, pos.add(dir.x * reach, dir.y * reach, dir.z * reach), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
        reach = (float) blocks.getHitVec().distanceTo(pos);
        Vector3d rangeVec = pos.add(dir.x * reach, dir.y * reach, dir.z * reach);
        Vector3d hitVec;
        List<Entity> list = entity.world.getEntitiesInAABBexcluding(entity,
                entity.getBoundingBox().expand(dir.x * reach, dir.y * reach, dir.z * reach).grow(1.0D),
                (t) -> EntityPredicates.NOT_SPECTATING.test(t) && t.canBeCollidedWith()
                        && (pred == null || pred.test(t)));
        for (Entity e : list) {
            AxisAlignedBB axisalignedbb = e.getBoundingBox().grow(e.getCollisionBorderSize());
            Optional<Vector3d> raytraceresult = axisalignedbb.rayTrace(pos, rangeVec);
            if(axisalignedbb.contains(pos)){
                return new EntityRayTraceResult(e, raytraceresult.orElse(pos));
            }
            if (raytraceresult.isPresent()) {
                double d3 = pos.distanceTo(raytraceresult.get());

                if (d3 < reach) {
                    hitVec = raytraceresult.get();
                    return new EntityRayTraceResult(e, hitVec);
                }
            }
        }
        return null;*/
    }

    /**
     * Returns a random collision free position around the given blockpos.
     * Null if it was not possible
     */
    @Nullable
    public static BlockPos randomPosAround(World world, Entity e, BlockPos pos, int range, boolean grounded, Random rand) {
        int randX = pos.getX() + rand.nextInt(2 * range) - range;
        int randY = pos.getY() + rand.nextInt(2 * range) - range;
        int randZ = pos.getZ() + rand.nextInt(2 * range) - range;
        if (!grounded) {
            BlockPos pos1 = new BlockPos(randX, randY, randZ);
            while (Math.abs(randY - pos1.getY()) < range && !world.isSpaceEmpty(e.getBoundingBox().offset(pos1))) {
                pos1 = pos1.up();
            }
            if (!world.isSpaceEmpty(e.getBoundingBox().offset(pos1)))
                return null;
            return pos1;
        }
        int y = pos.getY() - range;
        BlockPos pos1 = new BlockPos(randX, y, randZ);
        while (pos1.getY() - y < range && (!world.getBlockState(pos1.down()).isTopSolid(world, pos1.down(), e, Direction.UP)
                || !world.isSpaceEmpty(e.getBoundingBox().offset(pos1)))) {
            pos1 = pos1.up();
        }
        if (!world.isSpaceEmpty(e.getBoundingBox().offset(pos1)))
            return null;
        return pos1;
    }

    public static RayTraceResult entityRayTrace(Entity e, float range, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode,
                                                boolean includeEntities, @Nullable Predicate<Entity> pred) {
        Vector3d posEye = e.getEyePosition(1);
        Vector3d look = posEye.add(e.getLookVec().scale(range));
        if (includeEntities) {
            RayTraceResult raytraceresult = e.world.rayTraceBlocks(new RayTraceContext(posEye, look, blockMode, fluidMode, e));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                look = raytraceresult.getHitVec();
            }
            EntityRayTraceResult raytraceresult1 = ProjectileHelper.rayTraceEntities(e.world, e, posEye, look, e.getBoundingBox().expand(look).grow(1.0D), pred);
            if (raytraceresult1 != null) {
                raytraceresult = raytraceresult1;
            }
            return raytraceresult;
        }
        return e.world.rayTraceBlocks(new RayTraceContext(posEye, look, blockMode, fluidMode, e));
    }

    /*public static RayTraceResult entityRayTrace(Entity e, float range, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox,
            boolean returnLastUncollidableBlock) {
        return entityRayTrace(e, range, returnLastUncollidableBlock, returnLastUncollidableBlock, returnLastUncollidableBlock, false, null);
    }*/

    /**
     * Gets a list of vectors rotated around the given axis by the given angles
     *
     * @param dir    The vector to rotate
     * @param axis   The axis to rotate the vector around
     * @param minDeg Minimum rotation in degrees
     * @param maxDeg Maximum rotation in degrees
     * @param step   Angle change per rotation in degrees
     */
    public static List<Vector3f> rotatedVecs(Vector3d dir, Vector3d axis, float minDeg, float maxDeg, float step) {
        List<Vector3f> list = new ArrayList<>();
        Vector3f axisf = new Vector3f(axis);
        list.add(new Vector3f(dir));
        for (float y = step; y <= maxDeg; y += step) {
            list.add(rotatedAround(dir, axisf, y));
        }
        for (float y = minDeg; y <= -step; y += step) {
            list.add(rotatedAround(dir, axisf, y));
        }
        return list;
    }

    public static Vector3f rotatedAround(Vector3d dir, Vector3f axis, float deg) {
        Quaternion quaternion = new Quaternion(axis, deg, true);
        Vector3f newDir = new Vector3f(dir);
        newDir.func_214905_a(quaternion);
        return newDir;
    }
}
