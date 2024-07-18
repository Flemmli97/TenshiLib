package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class CircleSector {

    private final float theta, radius, rotAmount;

    private final Vec3 center, rotAxis, look;
    private final Entity entity;
    private final List<Vec3> vecs = new LinkedList<>();

    private PathNavigationRegion region;

    public CircleSector(Vec3 pos, Vec3 look, float radius, float theta, @Nullable Entity entity) {
        float amount = theta <= 10 ? 2 : Mth.ceil(theta / 5);
        this.rotAmount = MathUtils.degToRad(theta / amount);
        this.theta = MathUtils.degToRad(theta);
        this.radius = radius;
        this.rotAxis = MathUtils.rotate(MathUtils.rotate(new Vec3(0, 1, 0), new Vec3(1, 0, 0), (float) Mth.atan2(look.x, look.z)), look,
                -MathUtils.degToRad(90)).normalize();
        this.center = pos;
        this.look = look.normalize().scale(radius);
        this.entity = entity;
        if (this.entity != null) {
            this.createClipRegion(this.entity.level);
        }
        this.calculateRays();
    }

    /**
     * Calculate the rays for the raycasting to perform
     * Rays are used to prevent hits through walls
     */
    private void calculateRays() {
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

    /**
     * Create region for faster blockstate fetching since it allows direct chunk access
     */
    private void createClipRegion(Level level) {
        BlockPos center = new BlockPos(this.center);
        int dist = Mth.ceil(this.radius) + 4;
        this.region = new PathNavigationRegion(level, center.offset(-dist, -dist, -dist), center.offset(dist, dist, dist));
    }

    public boolean intersects(Level level, AABB aabb) {
        if (aabb.contains(this.center))
            return true;
        if (this.region == null) {
            this.createClipRegion(level);
        }
        for (Vec3 ray : this.vecs) {
            BlockHitResult blocks = this.region.clip(new ClipContext(this.center,
                    this.center.add(ray), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.entity));
            if (blocks.getType() != HitResult.Type.MISS)
                ray = blocks.getLocation();
            else
                ray = this.center.add(ray);
            if (aabb.clip(this.center, ray).isPresent())
                return true;
        }
        return false;
    }

    public void display(ServerLevel level) {
        if (this.region == null) {
            this.createClipRegion(level);
        }
        float rot = -this.theta;
        Vec3 ray = MathUtils.rotate(this.rotAxis, this.look, rot);
        while (rot <= this.theta) {
            BlockHitResult blocks = this.region.clip(new ClipContext(this.center,
                    this.center.add(ray), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.entity));
            float reach = (float) blocks.getLocation().distanceTo(this.center);
            Vec3 from = this.center;
            Vec3 to = ray.normalize().scale(reach).scale(0.1);
            for (int i = 0; i < 10; i++) {
                if (i == 9)
                    level.sendParticles(ParticleTypes.FLAME, from.x, from.y, from.z, 1, 0, 0, 0, 0);
                else
                    level.sendParticles(ParticleTypes.END_ROD, from.x, from.y, from.z, 1, 0, 0, 0, 0);
                from = from.add(to);
            }
            ray = MathUtils.rotate(this.rotAxis, this.look, rot += this.rotAmount);
        }
    }
}
