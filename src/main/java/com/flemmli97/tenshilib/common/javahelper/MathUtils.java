package com.flemmli97.tenshilib.common.javahelper;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtils {

	public static float degToRad(float degree)
	{
		return degree* ((float)Math.PI/180F);
	}
	
	public static float radToDeg(float rad)
	{
		return rad* (180/(float)Math.PI);
	}
	
	/**
	 * 
	 * @param radius
	 * @param density every x degree there will be a point (if possible)
	 * @return a list of x y array pair
	 */
	public static List<float[]> pointsOfCircle(float radius, int density)
	{
		float rad = degToRad(density);
		float i = -rad;
		List<float[]> list = Lists.newArrayList();
		while(i < 2*Math.PI)
		{
			i+=rad;
			list.add(new float[] {radius*MathHelper.cos(i), radius*MathHelper.sin(i)});
		}
		return list;
	}
	
	public static Vec3d closestPointToLine(Vec3d point, Vec3d l1, Vec3d dir)
	{
		return new Vec3d(MathHelper.clamp(point.x, l1.x, dir.x), 
				MathHelper.clamp(point.y, l1.y, dir.y),
				MathHelper.clamp(point.z, l1.z, dir.z));
	}
	
	public static Vec3d farestPointToLine(Vec3d point, Vec3d l1, Vec3d dir)
	{
		 return new Vec3d(Math.abs(l1.x-point.x)>Math.abs(dir.x-point.x)? l1.x : dir.x,
				 Math.abs(l1.y-point.y)>Math.abs(dir.y-point.y)?l1.y:dir.y, 
				 Math.abs(l1.z-point.z)>Math.abs(dir.z-point.z)?l1.z:dir.z);
	}
	
	public static Vec3d closestPointToAABB(Vec3d point, AxisAlignedBB aabb)	
	{
		return new Vec3d(MathHelper.clamp(point.x, aabb.minX, aabb.maxX), MathHelper.clamp(point.y, aabb.minY, aabb.maxY), MathHelper.clamp(point.z, aabb.minZ, aabb.maxZ));
	}
	
	public static Vec3d closestPointOnAABBToLine(AxisAlignedBB aabb, Vec3d line, Vec3d dir)
	{
		/*List<Pair<Vec3d,Vec3d>> list = Lists.newArrayList();
		list.add(Pair.of(new Vec3d(aabb.minX, aabb.minY, aabb.minZ), new Vec3d(aabb.maxX, aabb.minY, aabb.minZ)));
		list.add(Pair.of(new Vec3d(aabb.maxX, aabb.minY, aabb.minZ), new Vec3d(aabb.maxX, aabb.minY, aabb.maxZ)));
		list.add(Pair.of(new Vec3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vec3d(aabb.minX, aabb.minY, aabb.minZ)));
		list.add(Pair.of(new Vec3d(aabb.minX, aabb.minY, aabb.maxZ), new Vec3d(aabb.minX, aabb.minY, aabb.minZ)));
		
		list.add(Pair.of(new Vec3d(aabb.minX, aabb.minY, aabb.minZ), new Vec3d(aabb.minX, aabb.maxY, aabb.minZ)));
		list.add(Pair.of(new Vec3d(aabb.maxX, aabb.minY, aabb.minZ), new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ)));
		list.add(Pair.of(new Vec3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ)));
		list.add(Pair.of(new Vec3d(aabb.minX, aabb.minY, aabb.maxZ), new Vec3d(aabb.minX, aabb.maxY, aabb.maxZ)));

		list.add(Pair.of(new Vec3d(aabb.minX, aabb.maxY, aabb.minZ), new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ)));
		list.add(Pair.of(new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ)));
		list.add(Pair.of(new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ), new Vec3d(aabb.minX, aabb.maxY, aabb.minZ)));
		list.add(Pair.of(new Vec3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vec3d(aabb.minX, aabb.maxY, aabb.minZ)));*/
		
		List<Pair<Vec3d,Vec3d>> planes = Lists.newArrayList();
		Vec3d v1 = new Vec3d(aabb.minX, aabb.minY, aabb.minZ);
		planes.add(Pair.of(v1,v1.crossProduct(new Vec3d(aabb.maxX, aabb.minY, aabb.minZ))));
		Vec3d v2 = new Vec3d(aabb.minX, aabb.minY, aabb.minZ);
		planes.add(Pair.of(v2,v2.crossProduct(new Vec3d(aabb.minX, aabb.maxY, aabb.minZ))));
		Vec3d v3 = new Vec3d(aabb.maxX, aabb.minY, aabb.minZ);
		planes.add(Pair.of(v3,v3.crossProduct(new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ))));
		Vec3d v4 = new Vec3d(aabb.maxX, aabb.minY, aabb.maxZ);
		planes.add(Pair.of(v4,v4.crossProduct(new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ))));
		Vec3d v5 = new Vec3d(aabb.minX, aabb.minY, aabb.maxZ);
		planes.add(Pair.of(v5,v5.crossProduct(new Vec3d(aabb.minX, aabb.maxY, aabb.maxZ))));
		Vec3d v6 = new Vec3d(aabb.minX, aabb.maxY, aabb.minZ);
		planes.add(Pair.of(v6,v6.crossProduct(new Vec3d(aabb.maxX, aabb.maxY, aabb.minZ))));

		Vec3d point = line;
		
		for(Pair<Vec3d, Vec3d> pair : planes)
		{
			double dot = dir.dotProduct(pair.getRight());
			double val=pair.getLeft().subtract(line).dotProduct(pair.getRight());
			double dis;
			if(dot!=0)
			{
				
			}
			else
			{
				
			}
		}
		/*for(Pair<Vec3d, Vec3d> pair : list)
		{
			Vec3d point1 = closestPointOnTwoLines(pair.getLeft(), pair.getRight(), line, dir);
			if(point==null || closestPointToLine(point, line, dir).distanceTo(point)>closestPointToLine(point1, line, dir).distanceTo(point1))
				point=point1;
		}*/
		return point;
	}
}
