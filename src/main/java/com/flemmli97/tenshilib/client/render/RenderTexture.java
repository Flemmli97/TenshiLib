package com.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3f;

public abstract class RenderTexture<T extends Entity> extends EntityRenderer<T> {

    public final float xSize, ySize;
    private int red = 255;
    private int green = 255;
    private int blue = 255;
    private int alpha = 255;
    public final int rows, columns, length;
    public final float uLength, vLength;

    public RenderTexture(EntityRendererManager renderManager, float xSize, float ySize, int rows, int columns) {
        super(renderManager);
        this.xSize = xSize;
        this.ySize = ySize;
        this.rows = rows;
        this.columns = columns;
        this.length = rows * columns;
        this.uLength = 1F / columns;
        this.vLength = 1F / rows;
    }

    public void setColor(int hexColor) {
        this.setColor(hexColor >> 16 & 255, hexColor >> 8 & 255, hexColor & 255, hexColor >> 24 & 255);
    }

    public void setColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = alpha;
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight) {
        //if(!this.){
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180;
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        if (this.facePlayer()) {
            stack.multiply(this.renderManager.getRotation());
            stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
            //yaw = -this.renderManager.playerViewY + 180;
            //pitch = (this.renderManager.options.thirdPersonView == 2 ? 1 : -1) * this.renderManager.playerViewX;
        }
        float[] uvOffset = this.uvOffset(entity.ticksExisted);
        RenderUtils.renderTexture(stack, buffer, this.getEntityTexture(entity), 0, 0, 0, this.xSize, this.ySize, this.red, this.blue,
                this.green, this.alpha, yaw + this.yawOffset(), pitch + this.pitchOffset(), uvOffset[0], uvOffset[1], this.uLength, this.vLength);
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
        //}
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
        return new float[]{(frame % this.columns) * this.uLength, (frame / this.columns) * this.vLength};
    }
}
