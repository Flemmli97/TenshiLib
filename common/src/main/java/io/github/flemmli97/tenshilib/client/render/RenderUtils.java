package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Random;

public class RenderUtils {

    public static final int defaultColor = 0xFFFFFFFF;
    private static final float TRIANGLE_MULT = (float) (Math.sqrt(3.0D) / 2.0D);
    private static final Random RANDOM = new Random(432L);

    public static void renderBlockOutline(PoseStack matrixStack, MultiBufferSource buffer, Player player, BlockPos pos, float partialTicks, boolean drawImmediately) {
        renderBlockOutline(matrixStack, buffer, player, pos, partialTicks, 0, 0, 0, 1, drawImmediately);
    }

    /**
     * Renders the block shape at the given position
     *
     * @param drawImmediately Most of the time this should be true.
     *                        Else it will get drawn next frame and the position will be offset by player movement
     */
    public static void renderBlockOutline(PoseStack matrixStack, MultiBufferSource buffer, Player player, BlockPos pos, float partialTicks, float red, float green, float blue, float alpha,
                                          boolean drawImmediately) {
        BlockState state = player.level().getBlockState(pos);
        RenderType renderType = RenderType.lines();
        Vec3 vec = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        renderShape(matrixStack, buffer.getBuffer(renderType), state.getShape(player.level(), pos, CollisionContext.of(player)),
                pos.getX() - vec.x, pos.getY() - vec.y, pos.getZ() - vec.z, red, green, blue, alpha);
        if (drawImmediately && buffer instanceof MultiBufferSource.BufferSource)
            ((MultiBufferSource.BufferSource) buffer).endBatch(renderType);
    }

    public static void renderShape(PoseStack poseStack, VertexConsumer consumer, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((k, l, m, n, o, p) -> {
            float q = (float) (n - k);
            float r = (float) (o - l);
            float s = (float) (p - m);
            float t = Mth.sqrt(q * q + r * r + s * s);
            consumer.vertex(pose.pose(), (float) (k + x), (float) (l + y), (float) (m + z)).color(red, green, blue, alpha).normal(pose.normal(), q /= t, r /= t, s /= t).endVertex();
            consumer.vertex(pose.pose(), (float) (n + x), (float) (o + y), (float) (p + z)).color(red, green, blue, alpha).normal(pose.normal(), q, r, s).endVertex();
        });
    }

    public static void renderAreaAround(PoseStack matrixStack, MultiBufferSource buffer, BlockPos pos, float radius, boolean drawImmediately) {
        renderBoundingBox(matrixStack, buffer, new AABB(0, 0, 0, 1, 1, 1).inflate(radius).move(pos.below()), drawImmediately);
    }

    public static void renderAreaAround(PoseStack matrixStack, MultiBufferSource buffer, BlockPos pos, float radius, float red, float green, float blue,
                                        float alpha, boolean ignoreDepth, boolean drawImmediately) {
        renderBoundingBox(matrixStack, buffer, new AABB(0, 0, 0, 1, 1, 1).inflate(radius).move(pos.below()), red, green, blue,
                alpha, drawImmediately);
    }

    public static void renderBoundingBox(PoseStack matrixStack, MultiBufferSource buffer, AABB aabb, boolean drawImmediately) {
        RenderUtils.renderBoundingBox(matrixStack, buffer, aabb, 1, 0.5F, 0.5F, 1, drawImmediately);
    }

    /**
     * Renders the given bounding box similiar to entity hit boxes
     *
     * @param drawImmediately Most of the time this should be true.
     *                        Else it will get drawn next frame and the position will be offset by player movement
     */
    public static void renderBoundingBox(PoseStack matrixStack, MultiBufferSource buffer, AABB aabb, float red, float green, float blue, float alpha,
                                         boolean drawImmediately) {
        Vec3 vec = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        RenderType renderType = RenderType.lines();
        LevelRenderer.renderLineBox(matrixStack, buffer.getBuffer(renderType), aabb.inflate(0.002).move(-vec.x, -vec.y, -vec.z), red, green, blue, alpha);
        if (drawImmediately && buffer instanceof MultiBufferSource.BufferSource)
            ((MultiBufferSource.BufferSource) buffer).endBatch();
    }

    public static void applyYawPitch(PoseStack stack, float yaw, float pitch) {
        stack.mulPose(Axis.YP.rotationDegrees(yaw));
        stack.mulPose(Axis.ZP.rotationDegrees(pitch));
    }

    /**
     * Renders a texture
     *
     * @param textureBuilder Structure containing rendering info like color etc. Is mutable so cache an instance of it.
     */
    public static void renderTexture(PoseStack stack, VertexConsumer builder, float xSize, float ySize, TextureBuilder textureBuilder) {
        xSize = xSize / 2f;
        ySize = ySize / 2f;
        Matrix4f matrix4f = stack.last().pose();
        Matrix3f mat3f = stack.last().normal();
        builder.vertex(matrix4f, -xSize, ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).uv(textureBuilder.u, textureBuilder.v).overlayCoords(textureBuilder.overlay).uv2(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, xSize, ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).uv(textureBuilder.u + textureBuilder.uLength, textureBuilder.v).overlayCoords(textureBuilder.overlay).uv2(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, xSize, -ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).uv(textureBuilder.u + textureBuilder.uLength, textureBuilder.v + textureBuilder.vLength).overlayCoords(textureBuilder.overlay).uv2(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -xSize, -ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).uv(textureBuilder.u, textureBuilder.v + textureBuilder.vLength).overlayCoords(textureBuilder.overlay).uv2(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
    }

    public static void renderGradientBeams3d(PoseStack stack, MultiBufferSource renderTypeBuffer, float length, float width, int ticks, float partialTicks, float rotationPerTick, int amount, BeamBuilder builder) {
        stack.pushPose();
        RANDOM.setSeed(432L);
        for (int i = 0; i < amount; i++) {
            float ticker = ticks + partialTicks;
            stack.mulPose(Axis.XP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            stack.mulPose(Axis.YP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            stack.mulPose(Axis.ZP.rotationDegrees(RANDOM.nextFloat() * 360.0F + ticker * rotationPerTick));
            renderGradientBeam3d(stack, renderTypeBuffer, length, width, builder);
        }
        stack.popPose();
    }

    /**
     * Renders a gradient triangular cone shaped beam similar to the beams displayed during the enderdragons death
     *
     * @param builder Structure containing rendering info like color etc. Is mutable so cache an instance of it.
     */
    public static void renderGradientBeam3d(PoseStack stack, MultiBufferSource renderTypeBuffer, float length, float width, BeamBuilder builder) {
        float heightHalf = TRIANGLE_MULT * width * 0.5f;
        float widthHalf = width * 0.5f;
        Matrix4f matrix4f = stack.last().pose();
        VertexConsumer buffer = renderTypeBuffer.getBuffer(builder.renderType);
        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, -widthHalf, length, -heightHalf).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();
        buffer.vertex(matrix4f, widthHalf, length, -heightHalf).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();

        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, widthHalf, length, -heightHalf).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();
        buffer.vertex(matrix4f, 0, length, heightHalf).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();

        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, 0, length, heightHalf).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();
        buffer.vertex(matrix4f, -widthHalf, length, -heightHalf).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();
    }

    public static void renderGradientBeams(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float length, float width, int ticks, float partialTicks, float rotationPerTick, int amount, BeamBuilder builder) {
        matrixStack.pushPose();
        RANDOM.setSeed(432L);
        for (int i = 0; i < amount; i++) {
            float ticker = ticks + partialTicks;
            matrixStack.mulPose(Axis.XP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            matrixStack.mulPose(Axis.YP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            matrixStack.mulPose(Axis.ZP.rotationDegrees(RANDOM.nextFloat() * 360.0F + ticker * rotationPerTick));
            renderGradientBeam(matrixStack, renderTypeBuffer, length, width, builder);
        }
        matrixStack.popPose();
    }

    /**
     * Like {@link RenderUtils#renderGradientBeam3d} but 2d instead of cone shaped
     *
     * @param builder Structure containing rendering info like color etc. Is mutable so cache an instance of it.
     */
    public static void renderGradientBeam(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float length, float width, BeamBuilder builder) {
        float widthHalf = width * 0.5f;
        Matrix4f matrix4f = matrixStack.last().pose();
        VertexConsumer buffer = renderTypeBuffer.getBuffer(builder.renderType);
        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, -widthHalf, length, 0).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();
        buffer.vertex(matrix4f, widthHalf, length, 0).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();

        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, 0, 0, 0).color(builder.red, builder.green, builder.blue, builder.alpha).endVertex();
        buffer.vertex(matrix4f, widthHalf, length, 0).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();
        buffer.vertex(matrix4f, -widthHalf, length, 0).color(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha).endVertex();
    }

    public static class TextureBuilder {

        private int red = 255, green = 255, blue = 255, alpha = 255;
        private float u, v, uLength = 1, vLength = 1;
        private int light = 0xf000f0, overlay = OverlayTexture.NO_OVERLAY;

        public void setUV(float u, float v) {
            this.u = u;
            this.v = v;
        }

        public void setUVLength(float uLength, float vLength) {
            this.uLength = uLength;
            this.vLength = vLength;
        }

        public void setColor(int hexColor) {
            int red = hexColor >> 16 & 255;
            int green = hexColor >> 8 & 255;
            int blue = hexColor & 255;
            int alpha = hexColor >> 24 & 255;
            this.setColor(red, green, blue, alpha);
        }

        public void setColor(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public void setColor(float red, float green, float blue, float alpha) {
            this.setColor((int) red * 255, (int) green * 255, (int) blue * 255, (int) alpha * 255);
        }

        public void setOverlay(int overlay) {
            this.overlay = overlay;
        }

        public void setLight(int light) {
            this.light = light;
        }
    }

    public static class BeamBuilder {

        private int red = 255, green = 255, blue = 255, alpha = 255;
        private int endRed = 255, endGreen = 0, endBlue = 255, endAlpha = 0;
        private RenderType renderType = RenderType.lightning();

        public void setStartColor(int hexColor) {
            int red = hexColor >> 16 & 255;
            int green = hexColor >> 8 & 255;
            int blue = hexColor & 255;
            int alpha = hexColor >> 24 & 255;
            this.setStartColor(red, green, blue, alpha);
        }

        public void setStartColor(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public void setEndColor(int hexColor) {
            int red = hexColor >> 16 & 255;
            int green = hexColor >> 8 & 255;
            int blue = hexColor & 255;
            int alpha = hexColor >> 24 & 255;
            this.setEndColor(red, green, blue, alpha);
        }

        public void setEndColor(int red, int green, int blue, int alpha) {
            this.endRed = red;
            this.endGreen = green;
            this.endBlue = blue;
            this.endAlpha = alpha;
        }

        public void setRenderType(RenderType renderType) {
            this.renderType = renderType;
        }
    }
}
