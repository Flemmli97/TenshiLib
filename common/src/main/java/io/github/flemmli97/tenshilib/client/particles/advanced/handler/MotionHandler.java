package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.MotionData;
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
        particle.setParticleSpeed(this.data.delta().x(), this.data.delta().y(), this.data.delta().z());
    }
}
