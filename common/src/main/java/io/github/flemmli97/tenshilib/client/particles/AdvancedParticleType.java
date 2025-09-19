package io.github.flemmli97.tenshilib.client.particles;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;

/**
 * A ParticleRenderType that has an end hook that is run when all particles of this type is finished rendering.
 * Vanilla had this in the past but now its gone
 */
public interface AdvancedParticleType extends ParticleRenderType {

    void end(TextureManager textureManager);
}
