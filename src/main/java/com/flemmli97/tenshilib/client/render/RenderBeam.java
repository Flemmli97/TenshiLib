package com.flemmli97.tenshilib.client.render;

import com.flemmli97.tenshilib.api.entity.IBeamEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.ForgeRenderTypes;
import org.apache.commons.lang3.tuple.Pair;

public abstract class RenderBeam<T extends Entity & IBeamEntity> extends EntityRenderer<T> {

    protected final float radius;
    private int red = 255;
    private int green = 255;
    private int blue = 255;
    private int alpha = 255;

    public RenderBeam(EntityRendererManager manager, float width) {
        super(manager);
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
    public void render(T entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        entity.updateYawPitch();
        float dist = (float) entity.hitVec().distanceTo(entity.startVec());
        float width = this.widthFunc(entity);
        matrixStack.push();
        matrixStack.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) + 90));
        matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch)));
        matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(30));
        int layer = 3;
        if (entity.getShooter() == Minecraft.getInstance().player) {
            layer = 1;
            matrixStack.translate(0, -0.1, 0);
        }
        float startLength = 0;
        if (this.startTexture(entity) != null) {
            startLength = Math.min(this.startTexture(entity).size, dist * 0.5f);
            IVertexBuilder builder = buffer.getBuffer(this.getRenderLayer(entity, this.startTexture(entity).res));
            matrixStack.push();
            for (int i = 0; i < layer; i++) {
                matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(60));
                this.renderBeam(matrixStack, builder, width, startLength, 0, this.currentAnimation(entity, BeamPart.START), this.animationFrames(BeamPart.START), packedLight);
            }
            matrixStack.pop();
        }
        float length = dist - startLength;
        if (this.endTexture(entity) != null) {
            float endLength = Math.min(this.endTexture(entity).size, length);
            IVertexBuilder builder = buffer.getBuffer(this.getRenderLayer(entity, this.endTexture(entity).res));
            length -= endLength;
            matrixStack.push();
            for (int i = 0; i < layer; i++) {
                matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(60));
                this.renderBeam(matrixStack, builder, width, endLength, startLength + length, this.currentAnimation(entity, BeamPart.END),
                        this.animationFrames(BeamPart.END), packedLight);
            }
            matrixStack.pop();
        }
        IVertexBuilder builder = buffer.getBuffer(this.getRenderLayer(entity, this.getEntityTexture(entity)));
        float[] segments = this.segmentLength() == 0 ? new float[]{length} : this.split(length);
        for (int i = 0; i < layer; i++) {
            matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(60));
            for (int d = 0; d < segments.length; d++)
                this.renderBeam(matrixStack, builder, width, segments[d], startLength + d * this.segmentLength(), this.currentAnimation(entity, BeamPart.MIDDLE),
                        this.animationFrames(BeamPart.MIDDLE), packedLight);
        }
        matrixStack.pop();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    protected void renderBeam(MatrixStack stack, IVertexBuilder build, float width, float length, float offset, int animationFrame, float maxFrames, int light) {
        this.renderBeam(stack, build, width, length, offset, (animationFrame - 1) / maxFrames, animationFrame / maxFrames, light);
    }

    protected void renderBeam(MatrixStack stack, IVertexBuilder build, float width, float length, float offset, float vMin, float vMax, int light) {
        Matrix4f matrix4f = stack.peek().getModel();
        Matrix3f mat3f = stack.peek().getNormal();
        this.buildVertex(matrix4f, mat3f, build, offset, width, 0, 0, Math.max(0, vMin), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, offset + length, width, 0, 1, Math.max(0, vMin), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, offset + length, -width, 0, 1, Math.min(1, vMax), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, offset, -width, 0, 0, Math.min(1, vMax), 0, 0, 1, light);
    }

    protected void buildVertex(Matrix4f matrix4f, Matrix3f matrix3f, IVertexBuilder builder, float x, float y, float z, float u, float v, float nX, float nY, float nZ, int light) {
        builder.vertex(matrix4f, x, y, z).color(255, 255, 255, 255)
                .texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrix3f, nX, nY, nZ).endVertex();
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
        return (float) (this.radius * (Math.sin(Math.sqrt(entity.ticksExisted / (float) entity.livingTickMax()) * Math.PI)));
    }

    public float segmentLength() {
        return 0;
    }

    public int animationFrames(BeamPart part) {
        return 1;
    }

    public int currentAnimation(T entity, BeamPart part) {
        return entity.ticksExisted % this.animationFrames(part) + 1;
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
        return ForgeRenderTypes.getUnlitTranslucent(loc);
    }

    public static class ResourcePair {

        public final ResourceLocation res;
        public final float size;

        public ResourcePair(ResourceLocation res, float size) {
            this.res = res;
            this.size = size;
        }
    }

    public enum BeamPart {
        START, END, MIDDLE
    }
}
