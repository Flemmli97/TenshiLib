package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.CirclingData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class CirclingHandler implements AdvancedParticleHandler {

    private final CirclingData data;
    private final double initOffX, initOffY;

    private Vector3d last;
    private double angle, radius;
    private double centerX, centerY, centerZ;

    public CirclingHandler(CirclingData data, Particle particle) {
        this.data = data;
        Vec3 axis = data.rotationAxis().lengthSqr() < 0.0001 ? new Vec3(0, 1, 0) : data.rotationAxis().normalize();
        Vector3d offset = new Vector3d(-axis.y(), axis.x(), 0).normalize();
        this.initOffX = offset.x();
        this.initOffY = offset.y();
        this.radius = data.radius();
        this.angle = data.angle() * Mth.DEG_TO_RAD;
        this.centerX = ((ParticleAccessor) particle).getX();
        this.centerY = ((ParticleAccessor) particle).getY();
        this.centerZ = ((ParticleAccessor) particle).getZ();
        this.last = offset.add(this.centerX, this.centerY, this.centerZ);
        this.moveParticle(particle);
    }

    @Override
    public void tick(Particle particle) {
        if (this.data.radiusIncrease() == 0 && this.data.angleIncrease() == 0)
            return;
        this.moveParticle(particle);
    }

    protected void moveParticle(Particle particle) {
        Vector3d offset = new Vector3d(this.initOffX * this.radius, this.initOffY * this.radius, 0);
        offset.rotateAxis(this.angle, this.data.rotationAxis().x(), this.data.rotationAxis().y(), this.data.rotationAxis().z());
        double dX = ((ParticleAccessor) particle).getX() - this.last.x();
        double dY = ((ParticleAccessor) particle).getY() - this.last.y();
        double dZ = ((ParticleAccessor) particle).getZ() - this.last.z();
        this.centerX += dX;
        this.centerY += dY;
        this.centerZ += dZ;
        offset.add(this.centerX, this.centerY, this.centerZ);
        particle.setPos(offset.x(), offset.y(), offset.z());
        this.angle += this.data.angleIncrease() * Mth.DEG_TO_RAD;
        this.radius += this.data.radiusIncrease();
        this.last = offset;
    }
}
