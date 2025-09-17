package io.github.flemmli97.tenshilib.client.particles;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;

public interface AdvancedParticleType extends ParticleRenderType {

    void end(TextureManager textureManager);
}
