package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.SinMotionData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

public class SinMotionHandler implements AdvancedParticleHandler {

    private final SinMotionData data;
    private int tick;

    public SinMotionHandler(SinMotionData data, Particle particle) {
        this.data = data;
        particle.setParticleSpeed(data.delta().x(), data.delta().y(), data.delta().z());
    }

    @Override
    public void tick(Particle particle) {
        if (this.data.period() <= 0)
            return;
        this.tick++;
        float p = (this.tick + this.data.offset()) / this.data.period();
        Vec3 dir = this.data.delta().scale(Math.sin(p * Math.PI * 2));
        ParticleAccessor acc = (ParticleAccessor) particle;
        double dx = dir.x();
        double dy = dir.y();
        double dz = dir.z();
        if (this.data.add()) {
            dx += acc.getXD();
            dy += acc.getYD();
            dz += acc.getZD();
        }
        particle.setParticleSpeed(dx, dy, dz);
    }
}
