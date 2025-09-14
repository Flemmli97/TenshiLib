package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.ParticleMetaData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.particle.Particle;

public class ParticleMetaHandler implements AdvancedParticleHandler {

    public ParticleMetaHandler(ParticleMetaData data, Particle particle) {
        particle.setLifetime(data.duration());
        ((ParticleAccessor) particle).setHasPhysics(data.physics());
        ((ParticleAccessor) particle).setGravity(data.gravityScale());
    }
}
