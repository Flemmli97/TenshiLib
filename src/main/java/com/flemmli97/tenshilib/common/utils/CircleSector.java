package com.flemmli97.tenshilib.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class CircleSector {

    private final float theta, radius, rotAmount;

    private final Vector3d center, rotAxis, look;
    private final Entity entity;

    public CircleSector(Vector3d pos, Vector3d look, float radius, float theta, @Nullable Entity entity) {
        this.rotAmount = MathUtils.degToRad(theta > 120 ? 10 : theta > 30 ? 5 : 3);
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
        float rot = -this.theta;
        Vector3d ray = MathUtils.rotate(this.rotAxis, this.look, rot);
        while (rot <= this.theta) {
            BlockRayTraceResult blocks = world.rayTraceBlocks(new RayTraceContext(this.center,
                    this.center.add(ray.scale(this.radius)), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.entity));
            float reach = (float) blocks.getHitVec().distanceTo(this.center);
            if (aabb.rayTrace(this.center, this.center.add(ray.scale(reach))).isPresent())
                return true;
            ray = MathUtils.rotate(this.rotAxis, this.look, rot += this.rotAmount);
        }
        return false;
    }

    public void display(ServerWorld world){
        float rot = -this.theta;
        Vector3d ray = MathUtils.rotate(this.rotAxis, this.look, rot);
        while (rot <= this.theta) {
            BlockRayTraceResult blocks = world.rayTraceBlocks(new RayTraceContext(this.center,
                    this.center.add(ray.scale(this.radius)), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.entity));
            float reach = (float) blocks.getHitVec().distanceTo(this.center);
            Vector3d from = this.look;
            Vector3d to = ray.scale(reach).scale(0.1);
            for(int i = 0; i < 10; i++){
                if(i == 9)
                    world.spawnParticle(ParticleTypes.FLAME, from.x, from.y, from.z, 1,0,0,0,0);
                else
                    world.spawnParticle(ParticleTypes.END_ROD, from.x, from.y, from.z, 1,0,0,0,0);
                from = from.add(to);
            }
            ray = MathUtils.rotate(this.rotAxis, this.look, rot += this.rotAmount);
        }
    }
}
