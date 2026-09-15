package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.MotionData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;

public class MotionHandler implements AdvancedParticleHandler {

    private final MotionData data;

    public MotionHandler(MotionData data, Particle particle) {
        this.data = data;
        particle.setParticleSpeed(data.delta().x(), data.delta().y(), data.delta().z());
    }

    @Override
    public void tick(Particle particle) {
        if (!this.data.constant())
            return;
        ParticleAccessor acc = (ParticleAccessor) particle;
        double dx = this.data.delta().x();
        double dy = this.data.delta().y();
        double dz = this.data.delta().z();
        if (this.data.add()) {
            dx += acc.tenshilib$xd();
            dy += acc.tenshilib$yd();
            dz += acc.tenshilib$zd();
        }
        particle.setParticleSpeed(dx, dy, dz);
    }
}
