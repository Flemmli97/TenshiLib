package io.github.flemmli97.tenshilib.common.utils;

import io.github.flemmli97.tenshilib.common.utils.math.OrientedBoundingBox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class HitResultUtils {

    public static List<Entity> getEntities(LivingEntity entity, OrientedBoundingBox obb, Predicate<Entity> pred) {
        return getEntities(entity, obb, false, EntityTypeTest.forClass(Entity.class), pred);
    }

    public static <T extends Entity> List<T> getEntities(LivingEntity entity, OrientedBoundingBox obb, EntityTypeTest<Entity, T> typeTest, Predicate<T> pred) {
        return getEntities(entity, obb, false, typeTest, pred);
    }

    public static <T extends Entity> List<T> getEntities(LivingEntity entity, OrientedBoundingBox obb, boolean ignoreBlocks, EntityTypeTest<Entity, T> typeTest, Predicate<T> pred) {
        return obb.intersectingEntities(entity.level(), entity, ignoreBlocks, typeTest,
                t -> t != entity && (pred == null || pred.test(t)) && !t.isAlliedTo(entity) && t.isPickable());
    }

    public static EntityHitResult calculateEntityFromLook(LivingEntity entity, double reach) {
        return calculateEntityFromLook(entity, entity.getEyePosition(1), entity.getViewVector(1), reach, null);
    }

    public static EntityHitResult calculateEntityFromLook(LivingEntity entity, Vec3 pos, Vec3 dir, double reach,
                                                          @Nullable Predicate<Entity> pred) {
        Vec3 scaledDir = dir.scale(reach);
        EntityHitResult result = rayTraceEntities(entity, pos, pos.add(scaledDir), entity.getBoundingBox().expandTowards(scaledDir).inflate(1), (t) -> EntitySelector.NO_SPECTATORS.test(t) && t.isPickable()
                && (pred == null || pred.test(t)));
        if (result != null) {
            Vec3 loc = result.getLocation();
            double dist = pos.distanceToSqr(loc);
            if (dist <= reach * reach)
                return result;
        }
        return null;
    }

    public static HitResult entityRayTrace(Entity e, double range, ClipContext.Block blockMode, ClipContext.Fluid fluidMode,
                                           boolean includeEntities, boolean getEntityHitVec, @Nullable Predicate<Entity> pred) {
        if (pred == null)
            pred = entity -> true;
        Vec3 posEye = e.getEyePosition(1);
        Vec3 dir = e.getLookAngle().scale(range);
        Vec3 lookPos = posEye.add(dir);
        if (includeEntities) {
            HitResult raytraceresult = e.level().clip(new ClipContext(posEye, lookPos, blockMode, fluidMode, e));
            if (raytraceresult.getType() != HitResult.Type.MISS) {
                lookPos = raytraceresult.getLocation();
            }
            EntityHitResult entityHitResult;
            if (getEntityHitVec)
                entityHitResult = rayTraceEntities(e, posEye, lookPos, e.getBoundingBox().expandTowards(dir).inflate(1.0D), pred);
            else
                entityHitResult = ProjectileUtil.getEntityHitResult(e.level(), e, posEye, lookPos, e.getBoundingBox().expandTowards(dir).inflate(1.0D), pred);

            if (entityHitResult != null) {
                raytraceresult = entityHitResult;
            }
            return raytraceresult;
        }
        return e.level().clip(new ClipContext(posEye, lookPos, blockMode, fluidMode, e));
    }

    @Nullable
    public static EntityHitResult rayTraceEntities(Entity e, Vec3 from, Vec3 to, Predicate<Entity> pred) {
        return rayTraceEntities(e, from, to, e.getBoundingBox().expandTowards(e.getDeltaMovement()).inflate(1), pred);
    }

    @Nullable
    public static EntityHitResult rayTraceEntities(Entity e, Vec3 from, Vec3 to, AABB aabb, Predicate<Entity> pred) {
        return ProjectileUtil.getEntityHitResult(e, from, to, aabb, pred, Double.MAX_VALUE);
    }
}
