package io.github.flemmli97.tenshilib.client.particles;

import io.github.flemmli97.tenshilib.common.particle.ColoredParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

public class ColoredParticle extends TextureSheetParticle {

    public final SpriteSet spriteProvider;

    protected boolean randomMovements, gravity;

    public ColoredParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ,
                           ColoredParticleData colorData, SpriteSet sprite, int maxAge, float minAgeRand, float maxAgeRand,
                           boolean collide, boolean randomMovements, boolean gravity) {
        super(world, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.setColor(colorData.getRed(), colorData.getGreen(), colorData.getBlue());
        this.setAlpha(colorData.getAlpha());
        float mult = Mth.nextFloat(world.random, minAgeRand, maxAgeRand);
        this.lifetime = (int) (maxAge * mult);
        this.spriteProvider = sprite;
        this.setSpriteFromAge(this.spriteProvider);
        this.quadSize *= colorData.getScale();
        this.hasPhysics = collide;
        this.randomMovements = randomMovements;
        this.gravity = gravity;
    }

    public ColoredParticle setScale(float scale) {
        this.quadSize = scale;
        return this;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return this.quadSize * Mth.clamp(((float) this.age + partialTicks) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.TRANSLUCENTADD;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.spriteProvider);
            this.move(this.xd, this.yd, this.zd);
            if (this.randomMovements) {
                if (this.y == this.yo) {
                    this.xd *= 1.1D;
                    this.zd *= 1.1D;
                }

                this.xd *= 0.96F;
                this.yd *= 0.96F;
                this.zd *= 0.96F;
                if (this.onGround) {
                    this.xd *= 0.7F;
                    this.zd *= 0.7F;
                }
            }
            if (this.gravity) {
                this.yd = this.gravity();
                this.yd = Math.max(this.yd, this.maxGravity());
            }
        }
    }

    protected float gravity() {
        return -0.009f;
    }

    protected float maxGravity() {
        return -0.1f;
    }

    public static class LightParticleFactory implements ParticleProvider<ColoredParticleData> {

        private final SpriteSet sprite;

        public LightParticleFactory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(ColoredParticleData data, ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
            return new ColoredParticle(world, x, y, z, motionX, motionY, motionZ, data, this.sprite, 40, 0.7f, 1.3f, false, true, false);
        }
    }

    public static class NoGravityParticleFactory implements ParticleProvider<ColoredParticleData> {

        private final SpriteSet sprite;

        public NoGravityParticleFactory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(ColoredParticleData data, ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
            return new ColoredParticle(world, x, y, z, motionX, motionY, motionZ, data, this.sprite, 20, 1, 1, false, false, false).setScale(data.getScale());
        }
    }
}
