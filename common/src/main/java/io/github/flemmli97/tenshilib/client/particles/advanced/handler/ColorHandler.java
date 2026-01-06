package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.ColorData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import org.joml.Vector4f;

public class ColorHandler implements AdvancedParticleHandler {

    private final ColorData data;

    private Vector4f colorO, color;
    private int tick;

    public ColorHandler(ColorData data, Particle particle) {
        this.data = data;
        particle.setColor(data.start().x(), data.start().y(), data.start().z());
        this.colorO = new Vector4f(data.start());
        this.color = new Vector4f(data.start());
        ((ParticleAccessor) particle).setAlpha(data.start().w());
    }

    /**
     * We handle color setting in render tick for smooth transition as color is not interpolated by vanilla
     */
    @Override
    public void renderTick(Particle particle, float partialTick) {
        particle.setColor(Mth.lerp(partialTick, this.colorO.x(), this.color.x()),
                Mth.lerp(partialTick, this.colorO.y(), this.color.y()),
                Mth.lerp(partialTick, this.colorO.z(), this.color.z()));
        ((ParticleAccessor) particle).setAlpha(Mth.lerp(partialTick, this.colorO.w(), this.color.w()));
    }

    @Override
    public void tick(Particle particle) {
        if (this.data.duration() <= 0 || this.data.end().isEmpty())
            return;
        this.tick++;
        this.colorO = this.color;
        float prog = Mth.clamp((float) this.tick / this.data.duration(), 0, 1);
        Vector4f end = this.data.end().get();
        this.color = new Vector4f(Mth.lerp(prog, this.data.start().x(), end.x()),
                Mth.lerp(prog, this.data.start().y(), end.y()),
                Mth.lerp(prog, this.data.start().z(), end.z()),
                Mth.lerp(prog, this.data.start().w(), end.w()));
    }
}
