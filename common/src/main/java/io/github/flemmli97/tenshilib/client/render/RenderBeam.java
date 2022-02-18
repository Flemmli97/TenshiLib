package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.flemmli97.tenshilib.api.entity.IBeamEntity;
import io.github.flemmli97.tenshilib.platform.ClientCalls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public abstract class RenderBeam<T extends Entity & IBeamEntity> extends EntityRenderer<T> {

    protected final float radius;
    private int red = 255;
    private int green = 255;
    private int blue = 255;
    private int alpha = 255;

    public RenderBeam(EntityRendererProvider.Context ctx, float width) {
        super(ctx);
        this.radius = width;
    }

    public void setColor(int hexColor) {
        this.setColorAndAlpha(hexColor >> 16 & 255, hexColor >> 8 & 255, hexColor & 255, hexColor >> 24 & 255);
    }

    public void setColor(int red, int green, int blue) {
        this.red = red;
        this.blue = blue;
        this.green = green;
    }

    public void setColorAndAlpha(int red, int green, int blue, int alpha) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        entity.updateYawPitch();
        float dist = (float) entity.hitVec().distanceTo(entity.startVec());
        float width = this.widthFunc(entity);
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YN.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(30));
        int layer = 3;
        if (entity.getOwner() == Minecraft.getInstance().player) {
            layer = 1;
            matrixStack.translate(0, -0.1, 0);
        }
        float startLength = 0;
        if (this.startTexture(entity) != null) {
            startLength = Math.min(this.startTexture(entity).size, dist * 0.5f);
            VertexConsumer builder = buffer.getBuffer(this.getRenderLayer(entity, this.startTexture(entity).res));
            matrixStack.pushPose();
            for (int i = 0; i < layer; i++) {
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(60));
                this.renderBeam(matrixStack, builder, width, startLength, 0, this.currentAnimation(entity, BeamPart.START), this.animationFrames(BeamPart.START), packedLight);
            }
            matrixStack.popPose();
        }
        float length = dist - startLength;
        if (this.endTexture(entity) != null) {
            float endLength = Math.min(this.endTexture(entity).size, length);
            VertexConsumer builder = buffer.getBuffer(this.getRenderLayer(entity, this.endTexture(entity).res));
            length -= endLength;
            matrixStack.pushPose();
            for (int i = 0; i < layer; i++) {
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(60));
                this.renderBeam(matrixStack, builder, width, endLength, startLength + length, this.currentAnimation(entity, BeamPart.END),
                        this.animationFrames(BeamPart.END), packedLight);
            }
            matrixStack.popPose();
        }
        VertexConsumer builder = buffer.getBuffer(this.getRenderLayer(entity, this.getTextureLocation(entity)));
        float[] segments = this.segmentLength() == 0 ? new float[]{length} : this.split(length);
        for (int i = 0; i < layer; i++) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(60));
            for (int d = 0; d < segments.length; d++)
                this.renderBeam(matrixStack, builder, width, segments[d], startLength + d * this.segmentLength(), this.currentAnimation(entity, BeamPart.MIDDLE),
                        this.animationFrames(BeamPart.MIDDLE), packedLight);
        }
        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    protected void renderBeam(PoseStack stack, VertexConsumer build, float width, float length, float offset, int animationFrame, float maxFrames, int light) {
        this.renderBeam(stack, build, width, length, offset, (animationFrame - 1) / maxFrames, animationFrame / maxFrames, light);
    }

    protected void renderBeam(PoseStack stack, VertexConsumer build, float width, float length, float offset, float vMin, float vMax, int light) {
        Matrix4f matrix4f = stack.last().pose();
        Matrix3f mat3f = stack.last().normal();
        this.buildVertex(matrix4f, mat3f, build, offset, width, 0, 0, Math.max(0, vMin), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, offset + length, width, 0, 1, Math.max(0, vMin), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, offset + length, -width, 0, 1, Math.min(1, vMax), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, offset, -width, 0, 0, Math.min(1, vMax), 0, 0, 1, light);
    }

    protected void buildVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer builder, float x, float y, float z, float u, float v, float nX, float nY, float nZ, int light) {
        builder.vertex(matrix4f, x, y, z).color(255, 255, 255, 255)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, nX, nY, nZ).endVertex();
    }

    /**
     * Start texture of the beam. With size
     */
    public abstract ResourcePair startTexture(T entity);

    /**
     * End texture of the beam. With size
     */
    public abstract ResourcePair endTexture(T entity);

    public float widthFunc(T entity) {
        return (float) (this.radius * (Math.sin(Math.sqrt(entity.tickCount / (float) entity.livingTickMax()) * Math.PI)));
    }

    public float segmentLength() {
        return 0;
    }

    public int animationFrames(BeamPart part) {
        return 1;
    }

    public int currentAnimation(T entity, BeamPart part) {
        return entity.tickCount % this.animationFrames(part) + 1;
    }

    private float[] split(float length) {
        int arrL = (int) Math.ceil(length / this.segmentLength());
        float[] arr = new float[arrL];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Math.max(0, length - i * this.segmentLength());
        }
        return arr;
    }

    protected RenderType getRenderLayer(T entity, ResourceLocation loc) {
        return ClientCalls.instance().getBeamRenderType(loc);
    }

    public record ResourcePair(ResourceLocation res, float size) {

    }

    public enum BeamPart {
        START, END, MIDDLE
    }
}
