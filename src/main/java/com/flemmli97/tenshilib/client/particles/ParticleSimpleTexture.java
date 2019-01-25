package com.flemmli97.tenshilib.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class ParticleSimpleTexture extends Particle{

	public ParticleSimpleTexture(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture());
        float scale = 0.1F * this.particleScale;
        float posX = (float)(this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float posY = (float)(this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float posZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        Vec3d[] avec3d = new Vec3d[] {new Vec3d((-rotationX * scale - rotationXY * scale), (-rotationZ * scale), (-rotationYZ * scale - rotationXZ * scale)), new Vec3d((-rotationX * scale + rotationXY * scale), (rotationZ * scale), (-rotationYZ * scale + rotationXZ * scale)), new Vec3d((rotationX * scale + rotationXY * scale), (rotationZ * scale), (rotationYZ * scale + rotationXZ * scale)), new Vec3d((rotationX * scale - rotationXY * scale), (-rotationZ * scale), (rotationYZ * scale - rotationXZ * scale))};

        if (this.particleAngle != 0.0F)
        {
            float f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;
            float f9 = MathHelper.cos(f8 * 0.5F);
            float f10 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.x;
            float f11 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.y;
            float f12 = MathHelper.sin(f8 * 0.5F) * (float)cameraViewDir.z;
            Vec3d vec3d = new Vec3d(f10, f11, f12);

            for (int l = 0; l < 4; ++l)
            {
                avec3d[l] = vec3d.scale(2.0D * avec3d[l].dotProduct(vec3d)).add(avec3d[l].scale((f9 * f9) - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d[l]).scale((2.0F * f9)));
            }
        }

        buffer.pos(posX + avec3d[0].x, posY + avec3d[0].y, posZ + avec3d[0].z).tex(1, 1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(posX + avec3d[1].x, posY + avec3d[1].y, posZ + avec3d[1].z).tex(1, 0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(posX + avec3d[2].x, posY + avec3d[2].y, posZ + avec3d[2].z).tex(0, 0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(posX + avec3d[3].x, posY + avec3d[3].y, posZ + avec3d[3].z).tex(0, 1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }	
	
	@Override
	public int getFXLayer()
    {
        return 2;
    }
	public abstract ResourceLocation texture();
}
