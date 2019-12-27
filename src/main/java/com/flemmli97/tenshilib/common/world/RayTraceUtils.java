package com.flemmli97.tenshilib.common.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.common.javahelper.CircleSector;
import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RayTraceUtils {

	/**
	 * Gets a list of entities in a certain fov around the player
	 * @param entity
	 * @param reach Radius around the entity
	 * @param aoe FOV in degrees. 0 means vanilla raytracing. use 1 to have it like vanilla but get multiple entities.
	 * @return
	 */
	public static List<EntityLivingBase> getEntities(EntityLivingBase entity, float reach, float aoe)
	{
	    return getEntitiesIn(EntityLivingBase.class, entity, entity.getPositionVector().addVector(0, entity.height/2, 0),entity.getLook(1), reach, aoe, null);
	}
	
	public static <T extends EntityLivingBase> List<T> getEntitiesIn(Class<T> clss, EntityLivingBase entity, Vec3d pos, Vec3d look, float reach, float aoe, Predicate<T> pred){
	    CircleSector circ = new CircleSector(pos, look, reach, aoe);
	    return entity.world.getEntitiesWithinAABB(clss, entity.getEntityBoundingBox().grow(reach), 
            (living)->living!=entity && (pred==null||pred.apply(living)) && !living.isOnSameTeam(entity) && living.canBeCollidedWith() && circ.intersects(living.world, living.getEntityBoundingBox()));
	}
	
	public static RayTraceResult calculateEntityFromLook(EntityLivingBase entity, float reach)
	{
		return calculateEntityFromLook(entity, reach, Entity.class);
	}
	
	public static RayTraceResult calculateEntityFromLook(EntityLivingBase entity, float reach, Class<? extends Entity>clss)
	{
		return calculateEntityFromLook(entity, entity.getPositionEyes(1), entity.getLook(1), reach, clss,null);
	}
	
	public static RayTraceResult calculateEntityFromLook(EntityLivingBase entity, Vec3d pos, Vec3d dir, float reach, Class<? extends Entity>clss, @Nullable Predicate<? super Entity> pred)
    {
        RayTraceResult blocks = entity.world.rayTraceBlocks(pos, pos.addVector(dir.x * reach, dir.y * reach, dir.z * reach), false, false, true);
        reach=(float) blocks.hitVec.distanceTo(pos);
        Vec3d rangeVec = pos.addVector(dir.x * reach, dir.y * reach, dir.z * reach);
        Vec3d hitVec = null;
        List<Entity> list = entity.world.getEntitiesWithinAABB(clss, entity.getEntityBoundingBox().expand(dir.x * reach, dir.y * reach, dir.z * reach).expand(1.0D, 1.0D, 1.0D), (t)->EntitySelectors.NOT_SPECTATING.apply(t) && t != null && t!=entity && t.canBeCollidedWith() && (pred==null||pred.apply(t)));
        for(Entity e : list)
        {
            AxisAlignedBB axisalignedbb = e.getEntityBoundingBox().grow(e.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(pos, rangeVec);
            if (raytraceresult != null)
            {
                double d3 = pos.distanceTo(raytraceresult.hitVec);

                if (d3 < reach)
                {
                    hitVec = raytraceresult.hitVec;
                    return new RayTraceResult(e, hitVec);
                }
            }
        }
        return null;
    }
	
	public static BlockPos randomPosAround(World world, Entity e, BlockPos pos, int range, boolean grounded, Random rand)
	{
		int randX = pos.getX()+rand.nextInt(2*range)-range;
		int randY = pos.getY()+rand.nextInt(2*range)-range;
		int randZ = pos.getZ()+rand.nextInt(2*range)-range;
		if(!grounded)
		{
			BlockPos pos1 =  new BlockPos(randX, randY, randZ);
			while(Math.abs(randY-pos.getY())<range && world.collidesWithAnyBlock(e.getEntityBoundingBox().offset(pos1)))
			{
				pos1=pos1.up();
			}
			return pos1;
		}
		BlockPos pos1 =  new BlockPos(randX, 0, randZ);
		while(pos1.getY()<255 && (!world.getBlockState(pos1.down()).isSideSolid(world, pos1.down(), EnumFacing.UP) ||world.collidesWithAnyBlock(e.getEntityBoundingBox().offset(pos1))))
		{
			pos1=pos1.up();
		}
		return pos1;
	}
	
	public static RayTraceResult entityRayTrace(Entity e, float range, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, boolean includeEntities, @Nullable Predicate<? super Entity> pred)
    {
		Vec3d posEye = e.getPositionEyes(1);
		Vec3d look = posEye.add(e.getLookVec().scale(range));
		if(includeEntities)
		{
			Entity entity = null;
			double d0 = 0;
			List<Entity> list = e.world.getEntitiesInAABBexcluding(e, new AxisAlignedBB(e.posX,e.posY,e.posZ,e.posX+look.x,e.posY+look.y,e.posZ+look.z).grow(1), pred!=null?pred:EntitySelectors.NOT_SPECTATING);
			for (Entity entity1 : list)
	        {
	            if (entity1.canBeCollidedWith())
	            {
	                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
	                RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(posEye, look);
	                if (raytraceresult1 != null || axisalignedbb.contains(look))
	                {
	                    double d1 = raytraceresult1!=null?posEye.squareDistanceTo(raytraceresult1.hitVec):0;
	                    if (d1 < d0 || d0 == 0.0D)
	                    {
	                        entity = entity1;
	                        d0 = d1;
	                    }
	                }
	            }
	        }
			if(entity!=null)
				return new RayTraceResult(entity);
		}
		RayTraceResult blockpos = e.world.rayTraceBlocks(posEye, look, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		return blockpos!=null?blockpos:new RayTraceResult(RayTraceResult.Type.MISS, look, EnumFacing.getFacingFromVector((float)look.x, (float)look.y, (float)look.z),null);
    }
	
    public static RayTraceResult entityRayTrace(Entity e, float range, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
    {
		return entityRayTrace(e, range, returnLastUncollidableBlock, returnLastUncollidableBlock, returnLastUncollidableBlock, false, null);
    }
}

