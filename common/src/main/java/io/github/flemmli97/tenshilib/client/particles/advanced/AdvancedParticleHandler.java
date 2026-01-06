package io.github.flemmli97.tenshilib.client.particles.advanced;

import net.minecraft.client.particle.Particle;

/**
 * A particle handler.
 * Each particle initializes a new instance of this
 */
public interface AdvancedParticleHandler {

    default void tick(Particle particle) {
    }

    default void renderTick(Particle particle, float partialTick) {
    }
}
