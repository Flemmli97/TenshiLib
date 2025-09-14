package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.MoveToData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MoveToHandler implements AdvancedParticleHandler {

    private final MoveToData data;
    private final Vec3 start;
    private int tick;

    public MoveToHandler(MoveToData data, Particle particle) {
        this.data = data;
        this.start = new Vec3(((ParticleAccessor) particle).getX(), ((ParticleAccessor) particle).getY(), ((ParticleAccessor) particle).getZ());
    }

    @Override
    public void tick(Particle particle) {
        if (this.data.duration() <= 0)
            return;
        this.tick++;
        float prog = Mth.clamp((float) this.tick / this.data.duration(), 0, 1);
        Vec3 dir = this.data.target().subtract(this.start).scale(prog);
        Vec3 target = this.start.add(dir);
        particle.setPos(target.x(), target.y(), target.z());
    }
}
