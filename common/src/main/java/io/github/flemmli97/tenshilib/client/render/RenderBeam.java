package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.flemmli97.tenshilib.api.entity.IBeamEntity;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.client.CameraType;
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
    protected int alpha = 255;

    protected final float[][] points;
    protected final float[][] pointsGlow;

    public RenderBeam(EntityRendererProvider.Context ctx, float width) {
        this(ctx, width, 4);
    }

    public RenderBeam(EntityRendererProvider.Context ctx, float width, int polygon) {
        this(ctx, width, width - 0.25f, polygon);
    }

    public RenderBeam(EntityRendererProvider.Context ctx, float glowWidth, float innerWidth, int polygon) {
        super(ctx);
        this.radius = glowWidth;
        this.points = MathUtils.createRegularPolygonPointsF(polygon, innerWidth);
        this.pointsGlow = MathUtils.createRegularPolygonPointsF(polygon, glowWidth);
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
        boolean playerView = entity.getOwner() == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType() != CameraType.THIRD_PERSON_BACK;
        if (playerView) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(30));
            matrixStack.translate(0, -0.1, 0);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(60));
        } else
            matrixStack.scale(1, width, width);
        this.renderBeam(entity, dist, width, matrixStack, buffer, packedLight, playerView);
        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    protected void renderBeam(T entity, float dist, float width, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, boolean playerView) {
        float startLength = 0;
        // Start beam if texture specified
        int alphaPre;
        if (this.startTexture(entity) != null) {
            startLength = Math.min(this.startTexture(entity).size(), dist * 0.5f);
            if (playerView) {
                this.renderBeam(matrixStack, buffer.getBuffer(this.getRenderLayer(entity, this.startTexture(entity).res())), 0, startLength, width, -width, this.currentAnimation(entity, BeamPart.START), this.animationFrames(BeamPart.START), packedLight, true);
            } else {
                this.render3dBeam(matrixStack, buffer.getBuffer(this.getRenderLayer(entity, this.startTexture(entity).res())), this.points, 0, startLength, this.currentAnimation(entity, RenderBeam.BeamPart.MIDDLE), (float) this.animationFrames(RenderBeam.BeamPart.MIDDLE), packedLight);
                alphaPre = this.alpha;
                this.setAlpha(this.glowAlpha(entity));
                this.render3dBeam(matrixStack, buffer.getBuffer(this.getGlowingRenderLayer(entity, this.startTexture(entity).res())), this.pointsGlow, 0, startLength, this.currentAnimation(entity, RenderBeam.BeamPart.MIDDLE), (float) this.animationFrames(RenderBeam.BeamPart.MIDDLE), packedLight);
                this.setAlpha(alphaPre);
            }
        }
        float length = dist - startLength;
        // End beam if texture specified
        if (this.endTexture(entity) != null) {
            float endLength = Math.min(this.endTexture(entity).size(), length);
            length -= endLength;
            if (playerView) {
                this.renderBeam(matrixStack, buffer.getBuffer(this.getRenderLayer(entity, this.endTexture(entity).res())), startLength + length, endLength, width, -width, this.currentAnimation(entity, BeamPart.END), this.animationFrames(BeamPart.END), packedLight, true);
            } else {
                this.render3dBeam(matrixStack, buffer.getBuffer(this.getRenderLayer(entity, this.startTexture(entity).res())), this.points, startLength + length, endLength, this.currentAnimation(entity, RenderBeam.BeamPart.MIDDLE), (float) this.animationFrames(RenderBeam.BeamPart.MIDDLE), packedLight);
                alphaPre = this.alpha;
                this.setAlpha(this.glowAlpha(entity));
                this.render3dBeam(matrixStack, buffer.getBuffer(this.getGlowingRenderLayer(entity, this.startTexture(entity).res())), this.pointsGlow, startLength + length, endLength, this.currentAnimation(entity, RenderBeam.BeamPart.MIDDLE), (float) this.animationFrames(RenderBeam.BeamPart.MIDDLE), packedLight);
                this.setAlpha(alphaPre);
            }
        }
        // Render the main beam
        VertexConsumer builder = buffer.getBuffer(this.getRenderLayer(entity, this.getTextureLocation(entity)));
        float[] segments = this.segmentLength() == 0 ? new float[]{length} : this.split(length);
        for (int d = 0; d < segments.length; d++) {
            if (playerView) {
                this.renderBeam(matrixStack, builder, startLength + (float) d * this.segmentLength(), segments[d], width, -width, this.currentAnimation(entity, RenderBeam.BeamPart.MIDDLE), this.animationFrames(RenderBeam.BeamPart.MIDDLE), packedLight, true);
            } else {
                this.render3dBeam(matrixStack, builder, this.points, startLength + (float) d * this.segmentLength(), segments[d], this.currentAnimation(entity, RenderBeam.BeamPart.MIDDLE), this.animationFrames(RenderBeam.BeamPart.MIDDLE), packedLight);
            }
        }
        if (!playerView) {
            alphaPre = this.alpha;
            this.setAlpha(this.glowAlpha(entity));
            builder = buffer.getBuffer(this.getGlowingRenderLayer(entity, this.getTextureLocation(entity)));
            for (int d = 0; d < segments.length; d++) {
                this.render3dBeam(matrixStack, builder, this.pointsGlow, startLength + (float) d * this.segmentLength(), segments[d], this.currentAnimation(entity, RenderBeam.BeamPart.MIDDLE), (float) this.animationFrames(RenderBeam.BeamPart.MIDDLE), packedLight);
            }
            this.setAlpha(alphaPre);
        }
    }

    protected void render3dBeam(PoseStack stack, VertexConsumer build, float[][] corners, float minX, float length, int animationFrame, float maxFrames, int light) {
        Matrix4f matrix4f = stack.last().pose();
        Matrix3f mat3f = stack.last().normal();
        float vMin = (animationFrame - 1) / maxFrames;
        float vMax = animationFrame / maxFrames;
        for (int i = 0; i < corners.length; i++) {
            float[] point = corners[i];
            float[] next;
            if (i + 1 < corners.length)
                next = corners[i + 1];
            else
                next = corners[0];
            this.buildQuad(matrix4f, mat3f, build, minX, length, point[0], next[0], point[1], next[1], vMin, vMax, light, true);
        }
    }

    protected void renderBeam(PoseStack stack, VertexConsumer build, float minX, float length, float minY, float maxY, int animationFrame, float maxFrames, int light, boolean bothSided) {
        Matrix4f matrix4f = stack.last().pose();
        Matrix3f mat3f = stack.last().normal();
        this.buildQuad(matrix4f, mat3f, build, minX, length, minY, maxY, 0, 0, (animationFrame - 1) / maxFrames, animationFrame / maxFrames, light, bothSided);
    }

    protected void buildQuad(Matrix4f matrix4f, Matrix3f mat3f, VertexConsumer build, float minX, float length, float minY, float maxY, float minZ, float maxZ, float vMin, float vMax, int light, boolean bothSided) {
        this.buildVertex(matrix4f, mat3f, build, minX, minY, minZ, 0, Math.max(0, vMin), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, minX + length, minY, minZ, 1, Math.max(0, vMin), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, minX + length, maxY, maxZ, 1, Math.min(1, vMax), 0, 0, 1, light);
        this.buildVertex(matrix4f, mat3f, build, minX, maxY, maxZ, 0, Math.min(1, vMax), 0, 0, 1, light);
        if (bothSided) {
            this.buildVertex(matrix4f, mat3f, build, minX, maxY, maxZ, 0, Math.min(1, vMax), 0, 0, 1, light);
            this.buildVertex(matrix4f, mat3f, build, minX + length, maxY, maxZ, 1, Math.min(1, vMax), 0, 0, 1, light);
            this.buildVertex(matrix4f, mat3f, build, minX + length, minY, minZ, 1, Math.max(0, vMin), 0, 0, 1, light);
            this.buildVertex(matrix4f, mat3f, build, minX, minY, minZ, 0, Math.max(0, vMin), 0, 0, 1, light);
        }
    }

    protected void buildVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer builder, float x, float y, float z, float u, float v, float nX, float nY, float nZ, int light) {
        builder.vertex(matrix4f, x, y, z).color(this.red, this.green, this.blue, this.alpha)
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

    protected float[] split(float length) {
        int arrL = (int) Math.ceil(length / this.segmentLength());
        float[] arr = new float[arrL];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Math.max(0, length - i * this.segmentLength());
        }
        return arr;
    }

    protected RenderType getRenderLayer(T entity, ResourceLocation loc) {
        return RenderType.beaconBeam(loc, false);
    }

    protected RenderType getGlowingRenderLayer(T entity, ResourceLocation loc) {
        return RenderType.beaconBeam(loc, true);
    }

    protected int glowAlpha(T entity) {
        return 35;
    }

    public record ResourcePair(ResourceLocation res, float size) {
    }

    public enum BeamPart {
        START, END, MIDDLE
    }
}
