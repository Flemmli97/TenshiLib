package com.flemmli97.tenshilib.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.flemmli97.tenshilib.common.javahelper.MathUtils;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RayTraceUtils {

	/**
	 * Gets a list of entities in a certain fov around the player
	 * @param player
	 * @param reach Radius around the player
	 * @param aoe FOV in degrees
	 * @return
	 */
	public static List<EntityLivingBase> getEntities(EntityPlayer player, float reach, float aoe)
	{
		List<EntityLivingBase> list = new ArrayList<EntityLivingBase>();
		player.world.getEntitiesWithinAABB(EntityLivingBase.class, player.getEntityBoundingBox().grow(reach), new Predicate<EntityLivingBase>(){
			@Override
			public boolean apply(EntityLivingBase living) {
				return living!=player && !list.contains(living) && !living.isOnSameTeam(player) && living.canBeCollidedWith() && living.getDistanceSq(player.posX,living.posY,player.posZ) <= (reach*reach) && entityApplicable(player, living, reach, aoe);
			}}).forEach(entity->list.add(entity));
        return list;
	}
	
	private static boolean entityApplicable(EntityPlayer player, EntityLivingBase living, float reach, float fov)
	{
		Vec3d posVec = player.getPositionEyes(1);
		AxisAlignedBB axisalignedbb = living.getEntityBoundingBox().grow(living.getCollisionBorderSize());
		if(fov==0)
		{
			Vec3d look = player.getLook(1);
			RayTraceResult blocks = player.world.rayTraceBlocks(posVec, posVec.addVector(look.x * reach, look.y * reach, look.z * reach), false, false, true);
			reach=(float) blocks.hitVec.distanceTo(posVec);
			return axisalignedbb.calculateIntercept(posVec, posVec.addVector(look.x * reach, look.y * reach, look.z * reach))!=null;
		}
		if(axisalignedbb.contains(posVec))
			return true;
		while (player.rotationYaw > 360.0f) {
            player.rotationYaw -= 360.0f;
        }
        while (player.rotationYaw < -360.0f) {
            player.rotationYaw += 360.0f;
        }
		Vec3d point1 = new Vec3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
		Vec3d point2 = new Vec3d(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ);
		Vec3d point3 = new Vec3d(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ);
		Vec3d point6 = new Vec3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ);
		Vec3d point7 = new Vec3d(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ);
		Vec3d point8 = new Vec3d(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);

		//Test on X-Z Plane
		List<Pair<Vec3d,Vec3d>> lines = Lists.newArrayList();
		lines.add(Pair.of(point1, point6));
		lines.add(Pair.of(point2, point8));
		lines.add(Pair.of(point1, point7));
		lines.add(Pair.of(point3, point8));
		boolean xz=false;
		Vec3d closest = null;
		for(Pair<Vec3d, Vec3d> pair : lines)
		{
			closest = MathUtils.closestPointToLine(posVec, pair.getLeft(), pair.getRight());
			if(posVec.squareDistanceTo(closest)>(reach*reach))
				continue;
			double dx = closest.x - posVec.x;
	        double dz = closest.z - posVec.z;
	        if (dx == 0.0 && dz == 0.0)
	            dx = 0.001;
	        float yaw = (float)(MathHelper.atan2(dz, dx) * 180.0 / 3.141592653589793) - player.rotationYaw-90;
	        while (yaw < -180.0f) {
	            yaw += 360.0f;
	        }
	        while (yaw >= 180.0f) {
	            yaw -= 360.0f;
	        }
	        xz = yaw < fov && yaw > -fov;
	        if(xz)
	        	break;
		}
		if(!xz)
			return false;
		//Test pitch
		boolean y=false;
		for(Pair<Vec3d, Vec3d> pair : lines)
		{
			Vec3d vec = MathUtils.closestPointToLine(posVec, pair.getLeft(), pair.getRight());
			double dis = posVec.distanceTo(vec);
			double dy = living.posY-posVec.y;
			if (dy == 0.0)
				dy = 0.001;
			float pitchAOE = 20;
			float pitch = (float)(Math.acos(dy / dis) * 180.0 / 3.141592653589793)-(player.rotationPitch+90);
			y = pitch<pitchAOE && pitch>-pitchAOE;
			if(!y)
			{
				dy = living.posY+living.height-posVec.y;
				if (dy == 0.0)
					dy = 0.001;
				pitch = (float)(Math.acos(dy / dis) * 180.0 / 3.141592653589793)-(player.rotationPitch+90);
				y = pitch<pitchAOE && pitch>-pitchAOE;
			}
			if(y)
				break;
		}
		return y && canSeeEntity(posVec, living);
	}
	
	public static boolean canSeeEntity(Vec3d pos, EntityLivingBase living)
	{
		AxisAlignedBB aabb = living.getEntityBoundingBox().grow(living.getCollisionBorderSize());
		Vec3d corner = new Vec3d(aabb.minX, aabb.minY, aabb.minZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		corner = new Vec3d(aabb.minX, aabb.minY, aabb.maxZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		corner = new Vec3d(aabb.maxX, aabb.minY, aabb.minZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		corner = new Vec3d(aabb.maxX, aabb.minY, aabb.maxZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		corner = new Vec3d(aabb.minX, aabb.maxY, aabb.minZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		corner = new Vec3d(aabb.minX, aabb.maxY, aabb.maxZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		corner = new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		corner = new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ);
		if(living.world.rayTraceBlocks(pos, corner, false, true, false)==null)
			return true;
		return false;
	}
	
	public static RayTraceResult calculateEntityFromLook(EntityPlayer player, float reach)
	{
        Vec3d posVec = player.getPositionEyes(1);
		Vec3d look = player.getLook(1);
		RayTraceResult blocks = player.world.rayTraceBlocks(posVec, posVec.addVector(look.x * reach, look.y * reach, look.z * reach), false, false, true);
		reach=(float) blocks.hitVec.distanceTo(posVec);
        Vec3d rangeVec = posVec.addVector(look.x * reach, look.y * reach, look.z * reach);
        Vec3d hitVec = null;
        List<Entity> list = player.world.getEntitiesInAABBexcluding(player, player.getEntityBoundingBox().expand(look.x * reach, look.y * reach, look.z * reach).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
        {
            @Override
			public boolean apply(@Nullable Entity entity)
            {
                return entity != null && entity.canBeCollidedWith();
            }
        }));
        for(int i = 0; i < list.size(); ++i)
        {
        	Entity entity = list.get(i);
            AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(posVec, rangeVec);
            if (raytraceresult != null)
            {
                double d3 = posVec.distanceTo(raytraceresult.hitVec);

                if (d3 < reach)
                {
                    hitVec = raytraceresult.hitVec;
                    return new RayTraceResult(entity, hitVec);
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

