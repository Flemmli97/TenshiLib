package com.flemmli97.tenshilib.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;


public class RayTraceUtils {

    /**
     * Gets a list of entities in a certain fov around the player
     * @param entity
     * @param reach Radius around the entity
     * @param aoe FOV in degrees. 0 means vanilla raytracing. use 1 to have it like vanilla but get multiple entities.
     * @return
     */
    public static List<LivingEntity> getEntities(LivingEntity entity, float reach, float aoe) {
        return getEntitiesIn(LivingEntity.class, entity, entity.getPositionVec().add(0, entity.getHeight() / 2, 0), entity.getLook(1), reach,
                aoe, null);
    }

    public static <T extends LivingEntity> List<T> getEntitiesIn(Class<T> clss, LivingEntity entity, Vector3d pos, Vector3d look, float reach,
                                                                 float aoe, Predicate<T> pred) {
        CircleSector circ = new CircleSector(pos, look, reach, aoe, entity);
        return entity.world.getEntitiesWithinAABB(clss, entity.getBoundingBox().grow(reach),
                (living) -> living != entity && (pred == null || pred.test(living)) && !living.isOnSameTeam(entity) && living.canBeCollidedWith()
                        && circ.intersects(living.world, living.getBoundingBox()));
    }

    public static RayTraceResult calculateEntityFromLook(LivingEntity entity, float reach) {
        return calculateEntityFromLook(entity, reach, Entity.class);
    }

    public static RayTraceResult calculateEntityFromLook(LivingEntity entity, float reach, Class<? extends Entity> clss) {
        return calculateEntityFromLook(entity, entity.getEyePosition(1), entity.getLook(1), reach, clss, null);
    }

    public static RayTraceResult calculateEntityFromLook(LivingEntity entity, Vector3d pos, Vector3d dir, float reach, Class<? extends Entity> clss,
            @Nullable Predicate<? super Entity> pred) {
        RayTraceResult blocks = entity.world.rayTraceBlocks(new RayTraceContext(pos, pos.add(dir.x * reach, dir.y * reach, dir.z * reach), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
        reach = (float) blocks.getHitVec().distanceTo(pos);
        Vector3d rangeVec = pos.add(dir.x * reach, dir.y * reach, dir.z * reach);
        Vector3d hitVec = null;
        List<Entity> list = entity.world.getEntitiesWithinAABB(clss,
                entity.getBoundingBox().expand(dir.x * reach, dir.y * reach, dir.z * reach).expand(1.0D, 1.0D, 1.0D),
                (t) -> EntityPredicates.NOT_SPECTATING.test(t) && t != null && t != entity && t.canBeCollidedWith()
                        && (pred == null || pred.test(t)));
        for(Entity e : list){
            AxisAlignedBB axisalignedbb = e.getBoundingBox().grow(e.getCollisionBorderSize());
            Optional<Vector3d> raytraceresult = axisalignedbb.rayTrace(pos, rangeVec);
            if(raytraceresult.isPresent()){
                double d3 = pos.distanceTo(raytraceresult.get());

                if(d3 < reach){
                    hitVec = raytraceresult.get();
                    return new EntityRayTraceResult(e, hitVec);
                }
            }
        }
        return null;
    }

    public static BlockPos randomPosAround(World world, Entity e, BlockPos pos, int range, boolean grounded, Random rand) {
        int randX = pos.getX() + rand.nextInt(2 * range) - range;
        int randY = pos.getY() + rand.nextInt(2 * range) - range;
        int randZ = pos.getZ() + rand.nextInt(2 * range) - range;
        if(!grounded){
            BlockPos pos1 = new BlockPos(randX, randY, randZ);
            while(Math.abs(randY - pos.getY()) < range && world.getBlockCollisions(e, e.getBoundingBox().offset(pos1)).allMatch(VoxelShape::isEmpty)){
                pos1 = pos1.up();
            }
            return pos1;
        }
        BlockPos pos1 = new BlockPos(randX, 0, randZ);
        while(pos1.getY() < 255 && (!world.getBlockState(pos1.down()).isTopSolid(world, pos1.down(), e, Direction.UP))
                || world.getBlockCollisions(e, e.getBoundingBox().offset(pos1)).allMatch(VoxelShape::isEmpty)){
            pos1 = pos1.up();
        }
        return pos1;
    }

    public static RayTraceResult entityRayTrace(Entity e, float range, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode,
            boolean includeEntities, @Nullable Predicate<Entity> pred) {
        Vector3d posEye = e.getEyePosition(1);
        Vector3d look = posEye.add(e.getLookVec().scale(range));
        if(includeEntities){
            RayTraceResult raytraceresult = e.world.rayTraceBlocks(new RayTraceContext(posEye, look, blockMode, fluidMode, e));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                look = raytraceresult.getHitVec();
            }
            RayTraceResult raytraceresult1 = ProjectileHelper.rayTraceEntities(e.world, e, posEye, look, e.getBoundingBox().expand(look).grow(1.0D), pred);
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
}