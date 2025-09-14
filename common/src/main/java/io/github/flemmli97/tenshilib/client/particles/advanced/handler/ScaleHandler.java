package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.ScaleData;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;

public class ScaleHandler implements AdvancedParticleHandler {

    private final ScaleData data;
    private int tick;

    public ScaleHandler(ScaleData data, Particle particle) {
        this.data = data;
        this.setScaleParticle(particle, data.start());
    }

    @Override
    public void tick(Particle particle) {
        if (this.data.duration() <= 0)
            return;
        this.tick++;
        float prog = Mth.clamp((float) this.tick / this.data.duration(), 0, 1);
        this.setScaleParticle(particle, Mth.lerp(prog, this.data.start(), this.data.end()));
    }

    protected void setScaleParticle(Particle particle, float scale) {
        if (particle instanceof SingleQuadParticle quad) {
            float current = quad.getQuadSize(1);
            scale = scale / current;
        }
        particle.scale(scale);
    }
}
