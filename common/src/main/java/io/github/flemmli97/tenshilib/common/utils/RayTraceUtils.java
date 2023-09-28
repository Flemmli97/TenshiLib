package io.github.flemmli97.tenshilib.common.utils;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        return getEntitiesIn(entity, entity.position().add(0, entity.getEyeHeight(), 0), entity.getViewVector(1), reach,
                aoe, pred);
    }

    public static List<Entity> getEntitiesIgnorePitch(LivingEntity entity, float reach, float aoe, Predicate<Entity> pred) {
        return getEntitiesIn(entity, entity.position().add(0, 0.1, 0), Vec3.directionFromRotation(0, entity.getViewYRot(1)), reach,
                aoe, pred);
    }

    public static List<Entity> getEntitiesIn(LivingEntity entity, Vec3 pos, Vec3 look, float reach,
                                             float aoe, Predicate<Entity> pred) {
        CircleSector circ = new CircleSector(pos, look, reach, aoe, entity);
        return entity.level.getEntities(entity, entity.getBoundingBox().inflate(reach + 1),
                t -> t != entity && (pred == null || pred.test(t)) && !t.isAlliedTo(entity) && t.isPickable()
                        && circ.intersects(t.level, t.getBoundingBox().inflate(0.15, t.getBbHeight() <= 0.3 ? t.getBbHeight() : 0.15, 0.15)));
    }

    public static EntityHitResult calculateEntityFromLook(LivingEntity entity, float reach) {
        return calculateEntityFromLook(entity, entity.getEyePosition(1), entity.getViewVector(1), reach, null);
    }

    public static EntityHitResult calculateEntityFromLook(LivingEntity entity, Vec3 pos, Vec3 dir, float reach,
                                                          @Nullable Predicate<Entity> pred) {
        Vec3 scaledDir = dir.scale(reach);
        EntityHitResult result = rayTraceEntities(entity.level, entity, pos, pos.add(scaledDir), entity.getBoundingBox().expandTowards(scaledDir).inflate(1), (t) -> EntitySelector.NO_SPECTATORS.test(t) && t.isPickable()
                && (pred == null || pred.test(t)), Entity::getPickRadius);
        if (result != null) {
            Vec3 loc = result.getLocation();
            double dist = pos.distanceToSqr(loc);
            if (dist <= reach * reach)
                return result;
        }
        return null;
    }

    /**
     * Returns a random collision free position around the given blockpos.
     * Null if it was not possible
     */
    @Nullable
    public static BlockPos randomPosAround(Level world, Entity e, BlockPos pos, int range, boolean grounded, Random rand) {
        int randX = pos.getX() + rand.nextInt(2 * range) - range;
        int randY = pos.getY() + rand.nextInt(2 * range) - range;
        int randZ = pos.getZ() + rand.nextInt(2 * range) - range;
        if (!grounded) {
            BlockPos pos1 = new BlockPos(randX, randY, randZ);
            while (Math.abs(randY - pos1.getY()) < range && !world.noCollision(e.getBoundingBox().move(pos1))) {
                pos1 = pos1.above();
            }
            if (!world.noCollision(e.getBoundingBox().move(pos1)))
                return null;
            return pos1;
        }
        int y = pos.getY() - range;
        BlockPos pos1 = new BlockPos(randX, y, randZ);
        while (pos1.getY() - y < range && (!world.getBlockState(pos1.below()).entityCanStandOnFace(world, pos1.below(), e, Direction.UP)
                || !world.noCollision(e.getBoundingBox().move(pos1)))) {
            pos1 = pos1.above();
        }
        if (!world.noCollision(e.getBoundingBox().move(pos1)))
            return null;
        return pos1;
    }

    public static HitResult entityRayTrace(Entity e, float range, ClipContext.Block blockMode, ClipContext.Fluid fluidMode,
                                           boolean includeEntities, boolean getEntityHitVec, @Nullable Predicate<Entity> pred) {
        Vec3 posEye = e.getEyePosition(1);
        Vec3 dir = e.getLookAngle().scale(range);
        Vec3 lookPos = posEye.add(dir);
        if (includeEntities) {
            HitResult raytraceresult = e.level.clip(new ClipContext(posEye, lookPos, blockMode, fluidMode, e));
            if (raytraceresult.getType() != HitResult.Type.MISS) {
                lookPos = raytraceresult.getLocation();
            }
            EntityHitResult entityHitResult;
            if (getEntityHitVec)
                entityHitResult = rayTraceEntities(e.level, e, posEye, lookPos, e.getBoundingBox().expandTowards(dir).inflate(1.0D), pred, ent -> 0.3f);
            else
                entityHitResult = ProjectileUtil.getEntityHitResult(e.level, e, posEye, lookPos, e.getBoundingBox().expandTowards(dir).inflate(1.0D), pred == null ? entity -> true : pred);

            if (entityHitResult != null) {
                raytraceresult = entityHitResult;
            }
            return raytraceresult;
        }
        return e.level.clip(new ClipContext(posEye, lookPos, blockMode, fluidMode, e));
    }

    @Nullable
    public static EntityHitResult rayTraceEntities(Entity e, Vec3 from, Vec3 to, Predicate<Entity> pred) {
        return rayTraceEntities(e.level, e, from, to, e.getBoundingBox().expandTowards(e.getDeltaMovement()).inflate(1), pred, ent -> 0.3f);
    }

    /**
     * Like {@link ProjectileUtil#getEntityHitResult} but also saves the hit vector
     */
    @Nullable
    public static EntityHitResult rayTraceEntities(Level world, Entity e, Vec3 from, Vec3 to, AABB aabb, Predicate<Entity> pred, ToFloatFunction<Entity> inflateRadius) {
        double d0 = Double.MAX_VALUE;
        Entity entity = null;
        Vec3 hit = null;
        for (Entity entity1 : world.getEntities(e, aabb, pred)) {
            AABB axisalignedbb = entity1.getBoundingBox().inflate(inflateRadius.apply(entity1));
            if (axisalignedbb.contains(from)) {
                entity = entity1;
                hit = from;
                if (!axisalignedbb.contains(to))
                    hit = axisalignedbb.clip(from, to).orElse(from);
                break;
            }
            Optional<Vec3> optional = axisalignedbb.clip(from, to);
            if (optional.isPresent()) {
                if (d0 == 0) {
                    entity = entity1;
                    hit = optional.get();
                    break;
                }
                double d1 = from.distanceToSqr(optional.get());
                if (d1 < d0) {
                    entity = entity1;
                    d0 = d1;
                    hit = optional.get();
                }
            }
        }
        return entity == null ? null : new EntityHitResult(entity, hit);
    }

    /**
     * Gets the entity the projectile is hitting. Technically not a raytrace but a bounding box check.
     * Unused atm.
     * Problem: with radius > 1 can hit entities behind walls.
     *
     * @param entity The projectile entity. Also technically doesnt can be any Entity
     * @param from   Raytrace starting point
     * @param to     Raytrace end point
     * @param check  The AABB to check entitys in.
     * @param pred   Entity filter
     * @param radius Radius of the projectile collision.
     */
    public static EntityHitResult projectileRayTrace(Level world, Entity entity, Vec3 from, Vec3 to, AABB check, Predicate<Entity> pred, double radius) {
        double distVar = Double.MAX_VALUE;
        Entity ret = null;
        from = from.add(0, entity.getBbHeight() * 0.5, 0);
        Vec3 dir = to.subtract(from);
        AABB entityBB = entity.getBoundingBox();
        for (Entity entity1 : world.getEntities(entity, check, pred)) {
            AABB axisalignedbb = entity1.getBoundingBox().inflate(0.3F);
            Pair<Vec3, Vec3> points = MathUtils.closestPointsAABB(entityBB, axisalignedbb);
            double dist = points.getLeft().distanceToSqr(points.getRight());
            if (dist < distVar && dist <= radius * radius && (radius == 0 || MathUtils.isInFront(entity1.position(), from, dir))) {
                ret = entity1;
                distVar = dist;
            }
        }
        return ret == null ? null : new EntityHitResult(ret);
    }

    /**
     * Gets a list of vectors rotated around the given axis by the given angles
     *
     * @param dir    The vector to rotate
     * @param axis   The axis to rotate the vector around
     * @param minDeg Minimum rotation in degrees
     * @param maxDeg Maximum rotation in degrees
     * @param step   Angle change per rotation in degrees
     */
    public static List<Vector3f> rotatedVecs(Vec3 dir, Vec3 axis, float minDeg, float maxDeg, float step) {
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

    public static Vector3f rotatedAround(Vec3 dir, Vector3f axis, float deg) {
        Quaternion quaternion = new Quaternion(axis, deg, true);
        Vector3f newDir = new Vector3f(dir);
        newDir.transform(quaternion);
        return newDir;
    }
}
