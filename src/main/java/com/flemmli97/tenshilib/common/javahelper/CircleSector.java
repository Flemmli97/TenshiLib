package com.flemmli97.tenshilib.common.javahelper;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CircleSector {

    private final float theta, radius, rotAmount;

    private final Vec3d center, rotAxis, look;

    public CircleSector(Vec3d pos, Vec3d look, float radius, float theta) {
        this.rotAmount = MathUtils.degToRad(theta > 120 ? 10 : theta > 60 ? 5 : theta > 30 ? 2 : theta > 10 ? 1 : 0.5f);
        this.theta = MathUtils.degToRad(theta);
        this.radius = radius;
        this.rotAxis = MathUtils.rotate(MathUtils.rotate(new Vec3d(0, 1, 0), new Vec3d(1, 0, 0), (float) MathHelper.atan2(look.x, look.z)), look,
                -MathUtils.degToRad(90)).normalize();
        this.center = pos;
        this.look = look.normalize().scale(radius);
    }

    public boolean intersects(World world, AxisAlignedBB aabb) {
        if(aabb.contains(this.center))
            return true;
        aabb = aabb.grow(0.2);
        boolean flag = false;
        float rot = -this.theta;
        Vec3d ray = MathUtils.rotate(this.rotAxis, this.look, rot);
        while(!flag && rot <= this.theta){
            RayTraceResult blocks = world.rayTraceBlocks(this.center,
                    this.center.addVector(ray.x * this.radius, ray.y * this.radius, ray.z * this.radius), false, false, true);
            float reach = blocks != null ? (float) blocks.hitVec.distanceTo(this.center) : this.radius;
            flag = aabb.calculateIntercept(this.center, this.center.addVector(ray.x * reach, ray.y * reach, ray.z * reach)) != null;
            if(flag)
                break;
            ray = MathUtils.rotate(this.rotAxis, this.look, rot += this.rotAmount);
        }
        return flag;
    }

}
