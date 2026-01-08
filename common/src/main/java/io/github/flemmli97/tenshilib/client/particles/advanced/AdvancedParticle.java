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
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        this.handlers.forEach(d -> d.renderTick(this.wrapped, partialTick));
        this.wrapped.render(buffer, camera, partialTick);
    }

    @Override
    public void tick() {
        ParticleAccessor acc = (ParticleAccessor) this.getWrapped();
        double xo = acc.getX();
        double yo = acc.getY();
        double zo = acc.getZ();
        this.handlers.forEach(d -> d.tick(this.wrapped));
        this.wrapped.tick();
        acc.setXo(xo);
        acc.setYo(yo);
        acc.setZo(zo);
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
