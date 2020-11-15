package com.flemmli97.tenshilib.client.particles;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.TexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;

public abstract class ParticleSimpleTexture extends TexturedParticle {

    protected double uSize = 1;
    protected double vSize = 1;

    public ParticleSimpleTexture(ClientWorld world, double xCoordIn, double yCoordIn, double zCoordIn) {
        super(world, xCoordIn, yCoordIn, zCoordIn);
    }

    public ParticleSimpleTexture(ClientWorld world, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn,
                                 double zSpeedIn) {
        super(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }

    @Override
    public void buildGeometry(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Minecraft.getInstance().getTextureManager().bindTexture(this.texture());
        super.buildGeometry(buffer, renderInfo, partialTicks);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.CUSTOM;
    }

    public float getScale(float partialTicks) {
        return this.particleScale;
    }

    public double uOffset() {
        return 0;
    }

    public double vOffset() {
        return 0;
    }

    public abstract ResourceLocation texture();
}
