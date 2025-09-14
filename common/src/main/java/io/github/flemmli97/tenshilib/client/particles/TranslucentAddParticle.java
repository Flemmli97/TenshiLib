package io.github.flemmli97.tenshilib.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;

public class TranslucentAddParticle extends TextureSheetParticle {

    public final SpriteSet spriteProvider;

    protected boolean randomMovements, gravity;
    /**
     * The size of the particles texture.
     * This is important since we reduce the size of uv by one pixel to prevent texture atlas overflow from using a blurred texture (e.g. by using {@link ParticleRenderTypes#TRANSLUCENTADD}.
     * Set to 0 to disable
     */
    protected int textureSizeX = 16, textureSizeY = 16;

    public TranslucentAddParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
                                  SpriteSet sprite) {
        super(level, x, y, z, motionX, motionY, motionZ);
        this.spriteProvider = sprite;
        this.setSpriteFromAge(this.spriteProvider);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.TRANSLUCENTADD;
    }

    @Override
    protected float getU0() {
        if (this.textureSizeX <= 0)
            return super.getU0();
        float u0 = super.getU0();
        float u1 = super.getU1();
        return u0 + (u1 - u0) * (1f / this.textureSizeX);
    }

    @Override
    protected float getU1() {
        if (this.textureSizeX <= 0)
            return super.getU1();
        float u0 = super.getU0();
        float u1 = super.getU1();
        return u1 - (u1 - u0) * (1f / this.textureSizeX);
    }

    @Override
    protected float getV0() {
        if (this.textureSizeY <= 0)
            return super.getV0();
        float v0 = super.getV0();
        float v1 = super.getV1();
        return v0 + (v1 - v0) * (1f / this.textureSizeY);
    }

    @Override
    protected float getV1() {
        if (this.textureSizeY <= 0)
            return super.getV1();
        float v0 = super.getV0();
        float v1 = super.getV1();
        return v1 - (v1 - v0) * (1f / this.textureSizeY);
    }

    public static class Factory<T extends ParticleOptions> implements ParticleProvider<T> {

        private final SpriteSet sprite;

        public Factory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(T data, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
            return new TranslucentAddParticle(level, x, y, z, motionX, motionY, motionZ, this.sprite);
        }
    }
}
