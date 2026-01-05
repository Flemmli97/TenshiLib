package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.ScaleData;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;

public class ScaleHandler implements AdvancedParticleHandler {

    private final ScaleData data;

    private float scaleO, scale, partialTicks;
    private int tick;

    public ScaleHandler(ScaleData data, Particle particle) {
        this.data = data;
        this.setScaleParticle(particle, data.start());
        this.scaleO = this.scale;
    }

    /**
     * We handle scaling in render tick for smooth transition as scaling is not interpolated by vanilla
     * Since scaling affects the bounding box though the partial ticks is saved and used in the tick method too
     */
    @Override
    public void renderTick(Particle particle, float partialTicks) {
        this.partialTicks = partialTicks;
        particle.scale(Mth.lerp(partialTicks, this.scaleO, this.scale));
    }

    @Override
    public void tick(Particle particle) {
        if (this.data.duration() <= 0)
            return;
        this.tick++;
        this.scaleO = this.scale;
        float prog = Mth.clamp((float) this.tick / this.data.duration(), 0, 1);
        this.setScaleParticle(particle, Mth.lerp(prog, this.data.start(), this.data.end()));
    }

    protected void setScaleParticle(Particle particle, float scale) {
        if (particle instanceof SingleQuadParticle quad) {
            float current = quad.getQuadSize(1);
            scale = scale / current;
        }
        this.scale = scale;
        particle.scale(Mth.lerp(this.partialTicks, this.scaleO, this.scale));
    }
}
