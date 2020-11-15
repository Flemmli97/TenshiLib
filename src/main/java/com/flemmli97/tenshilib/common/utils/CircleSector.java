package com.flemmli97.tenshilib.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class CircleSector {

    private final float theta, radius, rotAmount;

    private final Vector3d center, rotAxis, look;
    private final Entity entity;

    public CircleSector(Vector3d pos, Vector3d look, float radius, float theta, @Nullable Entity entity) {
        this.rotAmount = MathUtils.degToRad(theta > 120 ? 10 : theta > 60 ? 5 : theta > 30 ? 2 : theta > 10 ? 1 : 0.5f);
        this.theta = MathUtils.degToRad(theta);
        this.radius = radius;
        this.rotAxis = MathUtils.rotate(MathUtils.rotate(new Vector3d(0, 1, 0), new Vector3d(1, 0, 0), (float) MathHelper.atan2(look.x, look.z)), look,
                -MathUtils.degToRad(90)).normalize();
        this.center = pos;
        this.look = look.normalize().scale(radius);
        this.entity = entity;
    }

    public boolean intersects(World world, AxisAlignedBB aabb) {
        if (aabb.contains(this.center))
            return true;
        aabb = aabb.grow(0.2);
        boolean flag = false;
        float rot = -this.theta;
        Vector3d ray = MathUtils.rotate(this.rotAxis, this.look, rot);
        while (rot <= this.theta) {
            BlockRayTraceResult blocks = world.rayTraceBlocks(new RayTraceContext(this.center,
                    this.center.add(ray.x * this.radius, ray.y * this.radius, ray.z * this.radius), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.entity));
            float reach = (float) blocks.getHitVec().distanceTo(this.center);
            flag = aabb.intersects(this.center, this.center.add(ray.x * reach, ray.y * reach, ray.z * reach));
            if (flag)
                break;
            ray = MathUtils.rotate(this.rotAxis, this.look, rot += this.rotAmount);
        }
        return flag;
    }

}
