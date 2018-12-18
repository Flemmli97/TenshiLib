package com.flemmli97.tenshilib.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
		if(fov==0)
		{
			Vec3d look = player.getLook(1);
			RayTraceResult blocks = player.world.rayTraceBlocks(posVec, posVec.addVector(look.x * reach, look.y * reach, look.z * reach), false, false, true);
			reach=(float) blocks.hitVec.distanceTo(posVec);
            AxisAlignedBB axisalignedbb = living.getEntityBoundingBox().grow(living.getCollisionBorderSize());
			return axisalignedbb.calculateIntercept(posVec, posVec.addVector(look.x * reach, look.y * reach, look.z * reach))!=null;
		}
		//Is entity in aoe area
		double dx = living.posX - player.posX;
        double dz = living.posZ - player.posZ;
        if (dx == 0.0 && dz == 0.0)
            dx = 0.001;
        while (player.rotationYaw > 360.0f) {
            player.rotationYaw -= 360.0f;
        }
        while (player.rotationYaw < -360.0f) {
            player.rotationYaw += 360.0f;
        }
        float yaw = (float)(Math.atan2(dz, dx) * 180.0 / 3.141592653589793) - player.rotationYaw;
        yaw -= 90.0f;

        while (yaw < -180.0f) {
            yaw += 360.0f;
        }
        while (yaw >= 180.0f) {
            yaw -= 360.0f;
        }
        double heightDiff = Math.min(Math.abs(posVec.y-living.posY), Math.abs(posVec.y-living.posY+living.height));
		boolean inAoe = yaw < fov && yaw > -fov && heightDiff<=2.5;
		return inAoe && canSeeEntity(posVec, living);
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
	
	public BlockPos randomPosAround(World world, Entity e, BlockPos pos, int range, boolean grounded, Random rand)
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
}
