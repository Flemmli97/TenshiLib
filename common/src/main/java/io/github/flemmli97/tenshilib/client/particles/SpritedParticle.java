package io.github.flemmli97.tenshilib.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;

public class SpritedParticle extends TextureSheetParticle {

    public final SpriteSet spriteProvider;
    private final ParticleRenderType renderType;

    public SpritedParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
                           SpriteSet sprite, ParticleRenderType renderType) {
        super(level, x, y, z, motionX, motionY, motionZ);
        this.spriteProvider = sprite;
        this.renderType = renderType;
        this.setSpriteFromAge(this.spriteProvider);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.renderType;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteProvider);
    }

    public static class Opaque<T extends ParticleOptions> implements ParticleProvider<T> {

        private final SpriteSet sprite;

        public Opaque(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(T data, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
            return new SpritedParticle(level, x, y, z, motionX, motionY, motionZ, this.sprite, ParticleRenderType.PARTICLE_SHEET_OPAQUE);
        }
    }

    public static class Translucent<T extends ParticleOptions> implements ParticleProvider<T> {

        private final SpriteSet sprite;

        public Translucent(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(T data, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
            return new SpritedParticle(level, x, y, z, motionX, motionY, motionZ, this.sprite, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT);
        }
    }
}
