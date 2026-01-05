package io.github.flemmli97.tenshilib.client.particles.advanced;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AdvancedParticle extends Particle {

    private final Particle wrapped;
    private final List<AdvancedParticleHandler> handlers;

    public AdvancedParticle(Particle wrapped, List<AdvancedParticleHandler> handlers, ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.wrapped = wrapped;
        ParticleAccessor acc = (ParticleAccessor) wrapped;
        acc.setXo(acc.getX());
        acc.setYo(acc.getY());
        acc.setZo(acc.getZ());
        this.handlers = handlers;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        this.handlers.forEach(d -> d.renderTick(this.wrapped, partialTicks));
        this.wrapped.render(buffer, camera, partialTicks);
    }

    @Override
    public void tick() {
        this.handlers.forEach(d -> d.tick(this.wrapped));
        this.wrapped.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.wrapped.getRenderType();
    }

    @Override
    public boolean isAlive() {
        return this.wrapped.isAlive();
    }

    @Override
    public AABB getBoundingBox() {
        if (this.wrapped == null)
            return super.getBoundingBox();
        return this.wrapped.getBoundingBox();
    }

    public Particle getWrapped() {
        return this.wrapped;
    }
}
