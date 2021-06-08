package com.flemmli97.tenshilib.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class CircleSector {

    private final float theta, radius, rotAmount;

    private final Vector3d center, rotAxis, look;
    private final Entity entity;
    private final List<Vector3d> vecs = new LinkedList<>();

    public CircleSector(Vector3d pos, Vector3d look, float radius, float theta, @Nullable Entity entity) {
        this.rotAmount = MathUtils.degToRad(theta > 120 ? 10 : theta > 30 ? 5 : 3);
        this.theta = MathUtils.degToRad(theta);
        this.radius = radius;
        this.rotAxis = MathUtils.rotate(MathUtils.rotate(new Vector3d(0, 1, 0), new Vector3d(1, 0, 0), (float) MathHelper.atan2(look.x, look.z)), look,
                -MathUtils.degToRad(90)).normalize();
        this.center = pos;
        this.look = look.normalize().scale(radius);
        this.entity = entity;
        this.calculateVecs();
    }

    private void calculateVecs() {
        this.vecs.add(this.look);
        float rot = -this.theta;
        while (rot < 0) {
            this.vecs.add(MathUtils.rotate(this.rotAxis, this.look, rot));
            rot += this.rotAmount;
        }
        rot = this.theta;
        while (rot > 0) {
            this.vecs.add(MathUtils.rotate(this.rotAxis, this.look, rot));
            rot -= this.rotAmount;
        }
    }

    public boolean intersects(World world, AxisAlignedBB aabb) {
        if (aabb.contains(this.center))
            return true;
        for (Vector3d ray : this.vecs) {
            BlockRayTraceResult blocks = world.rayTraceBlocks(new RayTraceContext(this.center,
                    this.center.add(ray), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.entity));
            if (blocks.getType() != RayTraceResult.Type.MISS)
                ray = blocks.getHitVec();
            else
                ray = this.center.add(ray);
            if (aabb.rayTrace(this.center, ray).isPresent())
                return true;
        }
        return false;
    }

    public void display(ServerWorld world) {
        float rot = -this.theta;
        Vector3d ray = MathUtils.rotate(this.rotAxis, this.look, rot);
        while (rot <= this.theta) {
            BlockRayTraceResult blocks = world.rayTraceBlocks(new RayTraceContext(this.center,
                    this.center.add(ray.scale(this.radius)), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.entity));
            float reach = (float) blocks.getHitVec().distanceTo(this.center);
            Vector3d from = this.look;
            Vector3d to = ray.scale(reach).scale(0.1);
            for (int i = 0; i < 10; i++) {
                if (i == 9)
                    world.spawnParticle(ParticleTypes.FLAME, from.x, from.y, from.z, 1, 0, 0, 0, 0);
                else
                    world.spawnParticle(ParticleTypes.END_ROD, from.x, from.y, from.z, 1, 0, 0, 0, 0);
                from = from.add(to);
            }
            ray = MathUtils.rotate(this.rotAxis, this.look, rot += this.rotAmount);
        }
    }
}
