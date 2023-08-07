package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class RenderTexture<T extends Entity> extends EntityRenderer<T> {

    public final float xSize, ySize;
    public final int rows, columns, length;
    public final float uLength, vLength;

    protected final RenderUtils.TextureBuilder textureBuilder = new RenderUtils.TextureBuilder();

    public RenderTexture(EntityRendererProvider.Context ctx, float xSize, float ySize, int rows, int columns) {
        super(ctx);
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
    public void render(T entity, float rotation, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        float yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks + 180;
        float pitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks;
        this.adjustYawPitch(stack, entity, partialTicks, yaw, pitch);
        float[] uvOffset = this.uvOffset(entity.tickCount);
        this.textureBuilder.setUV(uvOffset[0], uvOffset[1]);
        this.textureBuilder.setLight(packedLight);
        this.doRender(entity, partialTicks, stack, buffer);
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
    }

    public void doRender(T entity, float partialTicks, PoseStack stack, MultiBufferSource buffer) {
        RenderUtils.renderTexture(stack, buffer.getBuffer(this.getRenderType(entity, this.getTextureLocation(entity))), this.xSize, this.ySize, this.textureBuilder);
    }

    public void adjustYawPitch(PoseStack stack, T entity, float partialTicks, float yaw, float pitch) {
        if (this.facePlayer()) {
            stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            stack.mulPose(Axis.YP.rotationDegrees(180));
        } else {
            RenderUtils.applyYawPitch(stack, yaw + this.yawOffset(), -pitch + this.pitchOffset());
        }
    }

    protected RenderType getRenderType(T entity, ResourceLocation loc) {
        return RenderType.entityCutoutNoCull(loc);
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
