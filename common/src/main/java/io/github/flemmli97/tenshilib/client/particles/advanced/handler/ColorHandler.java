package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.ColorData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import org.joml.Vector4f;

public class ColorHandler implements AdvancedParticleHandler {

    private final ColorData data;
    private int tick;

    public ColorHandler(ColorData data, Particle particle) {
        this.data = data;
        particle.setColor(data.start().x(), data.start().y(), data.start().z());
        ((ParticleAccessor) particle).setAlpha(data.start().w());
    }

    @Override
    public void tick(Particle particle) {
        if (this.data.duration() <= 0 || this.data.end().isEmpty())
            return;
        this.tick++;
        float prog = Mth.clamp((float) this.tick / this.data.duration(), 0, 1);
        Vector4f end = this.data.end().get();
        particle.setColor(Mth.lerp(prog, this.data.start().x(), end.x()),
                Mth.lerp(prog, this.data.start().y(), end.y()),
                Mth.lerp(prog, this.data.start().z(), end.z()));
        ((ParticleAccessor) particle).setAlpha(Mth.lerp(prog, this.data.start().w(), end.w()));
    }
}
