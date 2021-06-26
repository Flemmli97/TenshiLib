package com.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public abstract class RenderTexture<T extends Entity> extends EntityRenderer<T> {

    public final float xSize, ySize;
    public final int rows, columns, length;
    public final float uLength, vLength;

    protected final RenderUtils.TextureBuilder textureBuilder = new RenderUtils.TextureBuilder();

    public RenderTexture(EntityRendererManager renderManager, float xSize, float ySize, int rows, int columns) {
        super(renderManager);
        this.xSize = xSize;
        this.ySize = ySize;
        this.rows = rows;
        this.columns = columns;
        this.length = rows * columns;
        this.uLength = 1F / columns;
        this.vLength = 1F / rows;
        this.textureBuilder.setUVLength(this.uLength, this.vLength);
    }

    public void setColor(int hexColor) {
        this.textureBuilder.setColor(hexColor);
    }

    public void setColor(int red, int green, int blue, int alpha) {
        this.textureBuilder.setColor(red, green, blue, alpha);
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight) {
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        if (this.facePlayer()) {
            stack.rotate(this.renderManager.getCameraOrientation());
            stack.rotate(Vector3f.YP.rotationDegrees(180));
        } else {
            RenderUtils.applyYawPitch(stack, yaw + this.yawOffset(), pitch + this.pitchOffset());
        }
        float[] uvOffset = this.uvOffset(entity.ticksExisted);
        this.textureBuilder.setUV(uvOffset[0], uvOffset[1]);
        this.textureBuilder.setLight(packedLight);
        RenderUtils.renderTexture(stack, buffer.getBuffer(this.getRenderType(entity, this.getEntityTexture(entity))), this.xSize, this.ySize, this.textureBuilder);
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
    }

    protected RenderType getRenderType(T entity, ResourceLocation loc) {
        return RenderType.getEntityCutoutNoCull(loc);
    }

    public boolean facePlayer() {
        return true;
    }

    public float yawOffset() {
        return 0;
    }

    public float pitchOffset() {
        return 0;
    }

    public float[] uvOffset(int timer) {
        int frame = timer % this.length;
        return new float[]{(frame % this.columns) * this.uLength, (frame / (float) this.columns) * this.vLength};
    }
}
