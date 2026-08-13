package io.github.flemmli97.tenshilib.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.client.VertexUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TranslucentAddParticle extends TextureSheetParticle {

    public final SpriteSet spriteProvider;

    public TranslucentAddParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
                                  SpriteSet sprite) {
        super(level, x, y, z, motionX, motionY, motionZ);
        this.spriteProvider = sprite;
        this.setSpriteFromAge(this.spriteProvider);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.TRANSLUCENT_ADD_BLURRED;
    }

    @Override
    protected void renderRotatedQuad(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float partialTicks) {
        float size = this.getQuadSize(partialTicks);
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);
        VertexUtils.addVertexData(this.renderVertex(buffer, quaternion, x, y, z, 1.0F, -1.0F, size, u1, v1, light),
                VertexUtils.VEC4f.get(), u0, v0, u1, v1);
        VertexUtils.addVertexData(this.renderVertex(buffer, quaternion, x, y, z, 1.0F, 1.0F, size, u1, v0, light),
                VertexUtils.VEC4f.get(), u0, v0, u1, v1);
        VertexUtils.addVertexData(this.renderVertex(buffer, quaternion, x, y, z, -1.0F, 1.0F, size, u0, v0, light),
                VertexUtils.VEC4f.get(), u0, v0, u1, v1);
        VertexUtils.addVertexData(this.renderVertex(buffer, quaternion, x, y, z, -1.0F, -1.0F, size, u0, v1, light),
                VertexUtils.VEC4f.get(), u0, v0, u1, v1);
    }

    private VertexConsumer renderVertex(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float xOffset, float yOffset, float quadSize, float u, float v, int packedLight) {
        Vector3f vector3f = (new Vector3f(xOffset, yOffset, 0.0F)).rotate(quaternion).mul(quadSize).add(x, y, z);
        return buffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z()).setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(packedLight);
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
