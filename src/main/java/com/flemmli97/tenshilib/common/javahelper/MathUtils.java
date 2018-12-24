package com.flemmli97.tenshilib.common.javahelper;

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
}
