package com.flemmli97.tenshilib.common.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RayTraceUtils {

	public static RayTraceResult rayTraceBlock(EntityPlayer player)
	{
		return null;
	}
	
	public static RayTraceResult calculateEntityFromLook(EntityPlayer player, float reach)
	{
		RayTraceResult result = null;
		if(player.world!=null)
		{
			Entity entity = null;
            Vec3d posVec = player.getPositionEyes(1);
			Vec3d look = player.getLook(1);
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
            	Entity entity1 = (Entity)list.get(i);
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double)entity1.getCollisionBorderSize());
                RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(posVec, rangeVec);
                if (raytraceresult != null)
                {
                    double d3 = posVec.distanceTo(raytraceresult.hitVec);

                    if (d3 < reach)
                    {
                        entity = entity1;
                        hitVec = raytraceresult.hitVec;
                    }
                }
            }
            if(entity!=null)
            {
            	result = new RayTraceResult(entity, hitVec);
            }
		}
		return result;
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
