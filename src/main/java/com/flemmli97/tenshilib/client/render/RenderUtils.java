package com.flemmli97.tenshilib.client.render;

import com.flemmli97.tenshilib.mixin.WorldRenderAccessor;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {

    public static final int defaultColor = 0xFFFFFFFF;
    private static final float triangleMult = (float) (Math.sqrt(3.0D) / 2.0D);
    private static final Random random = new Random(432L);

    public static void renderBlockOutline(MatrixStack matrixStack, IRenderTypeBuffer buffer, PlayerEntity player, BlockPos pos, float partialTicks, boolean drawImmediately) {
        renderBlockOutline(matrixStack, buffer, player, pos, partialTicks, 0, 0, 0, 1, false, drawImmediately);
    }

    /**
     * Renders the block shape at the given position
     *
     * @param ignoreDepth     Doesnt work atm
     * @param drawImmediately Most of the time this should be true.
     *                        Else it will get drawn next frame and the position will be offset by player movement
     */
    public static void renderBlockOutline(MatrixStack matrixStack, IRenderTypeBuffer buffer, PlayerEntity player, BlockPos pos, float partialTicks, float red, float green, float blue, float alpha,
                                          boolean ignoreDepth, boolean drawImmediately) {
        BlockState state = player.world.getBlockState(pos);
        RenderType renderType;
        if (ignoreDepth) {
            renderType = MoreRenderTypes.LINE_NODEPTH;
        } else {
            renderType = RenderType.getLines();
        }
        Vector3d vec = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        WorldRenderAccessor.drawShapeOutline(matrixStack, buffer.getBuffer(renderType), state.getShape(player.world, pos, ISelectionContext.forEntity(player)),
                pos.getX() - vec.x, pos.getY() - vec.y, pos.getZ() - vec.z, red, green, blue, alpha);
        if (drawImmediately && buffer instanceof IRenderTypeBuffer.Impl)
            ((IRenderTypeBuffer.Impl) buffer).finish(renderType);
    }

    public static void renderAreaAround(MatrixStack matrixStack, IRenderTypeBuffer buffer, BlockPos pos, float radius, boolean drawImmediately) {
        renderBoundingBox(matrixStack, buffer, new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius).offset(pos.down()), drawImmediately);
    }

    public static void renderAreaAround(MatrixStack matrixStack, IRenderTypeBuffer buffer, BlockPos pos, float radius, float red, float green, float blue,
                                        float alpha, boolean ignoreDepth, boolean drawImmediately) {
        renderBoundingBox(matrixStack, buffer, new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius).offset(pos.down()), red, green, blue,
                alpha, ignoreDepth, drawImmediately);
    }

    public static void renderBoundingBox(MatrixStack matrixStack, IRenderTypeBuffer buffer, AxisAlignedBB aabb, boolean drawImmediately) {
        RenderUtils.renderBoundingBox(matrixStack, buffer, aabb, 1, 0.5F, 0.5F, 1, false, drawImmediately);
    }

    /**
     * Renders the given bounding box similiar to entity hit boxes
     *
     * @param ignoreDepth     Doesnt work atm
     * @param drawImmediately Most of the time this should be true.
     *                        Else it will get drawn next frame and the position will be offset by player movement
     */
    public static void renderBoundingBox(MatrixStack matrixStack, IRenderTypeBuffer buffer, AxisAlignedBB aabb, float red, float green, float blue, float alpha,
                                         boolean ignoreDepth, boolean drawImmediately) {
        Vector3d vec = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        RenderType renderType;
        if (ignoreDepth) {
            renderType = MoreRenderTypes.LINE_NODEPTH;
        } else {
            renderType = RenderType.getLines();
        }
        WorldRenderer.drawBoundingBox(matrixStack, buffer.getBuffer(renderType), aabb.grow(0.002).offset(-vec.x, -vec.y, -vec.z), red, green, blue, alpha);
        if (drawImmediately && buffer instanceof IRenderTypeBuffer.Impl)
            ((IRenderTypeBuffer.Impl) buffer).finish();
    }

    public static void applyYawPitch(MatrixStack stack, float yaw, float pitch) {
        stack.rotate(Vector3f.YP.rotationDegrees(yaw));
        stack.rotate(Vector3f.XP.rotationDegrees(pitch));
    }

    /**
     * Renders a texture
     *
     * @param stack
     * @param builder
     * @param xSize
     * @param ySize
     * @param textureBuilder Structure containing rendering info like color etc. mutable. Cache an instance of it.
     */
    public static void renderTexture(MatrixStack stack, IVertexBuilder builder, float xSize, float ySize, TextureBuilder textureBuilder) {
        xSize = xSize / 2f;
        ySize = ySize / 2f;
        Matrix4f matrix4f = stack.getLast().getMatrix();
        Matrix3f mat3f = stack.getLast().getNormal();
        builder.pos(matrix4f, -xSize, ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).tex(textureBuilder.u, textureBuilder.v).overlay(textureBuilder.overlay).lightmap(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
        builder.pos(matrix4f, xSize, ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).tex(textureBuilder.u + textureBuilder.uLength, textureBuilder.v).overlay(textureBuilder.overlay).lightmap(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
        builder.pos(matrix4f, xSize, -ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).tex(textureBuilder.u + textureBuilder.uLength, textureBuilder.v + textureBuilder.vLength).overlay(textureBuilder.overlay).lightmap(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
        builder.pos(matrix4f, -xSize, -ySize, 0).color(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).tex(textureBuilder.u, textureBuilder.v + textureBuilder.vLength).overlay(textureBuilder.overlay).lightmap(textureBuilder.light).normal(mat3f, 0, 0, 1).endVertex();
    }

    public static void renderGradientBeams3d(MatrixStack stack, IRenderTypeBuffer renderTypeBuffer, float length, float width, int ticks, float partialTicks, int amount, int endRed, int endGreen, int endBlue, int endAlpha) {
        stack.push();
        random.setSeed(432L);
        for (int i = 0; i < amount; i++) {
            stack.rotate(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F + partialTicks));
            stack.rotate(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F + partialTicks));
            stack.rotate(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + partialTicks));
            float ticker = (ticks + partialTicks) / 200;
            int alpha = (int) (255.0F * (1.0F - ticker));
            renderGradientBeam3d(stack, renderTypeBuffer, length, width, 255, 255, 255, alpha, endRed, endGreen, endBlue, endAlpha);
        }
        stack.pop();
    }

    public static void renderGradientBeam3d(MatrixStack stack, IRenderTypeBuffer renderTypeBuffer, float length, float width, int endRed, int endGreen, int endBlue, int endAlpha) {
        renderGradientBeam3d(stack, renderTypeBuffer, length, width, 255, 255, 255, 255, endRed, endGreen, endBlue, endAlpha);
    }

    public static void renderGradientBeam3d(MatrixStack stack, IRenderTypeBuffer renderTypeBuffer, float length, float width, int alpha, int endRed, int endGreen, int endBlue, int endAlpha) {
        renderGradientBeam3d(stack, renderTypeBuffer, length, width, 255, 255, 255, alpha, endRed, endGreen, endBlue, endAlpha);
    }

    /**
     * Renders a gradient triangular cone shaped beam similar to the beams displayed during the enderdragons death
     */
    public static void renderGradientBeam3d(MatrixStack stack, IRenderTypeBuffer renderTypeBuffer, float length, float width,
                                            int red, int green, int blue, int alpha, int endRed, int endGreen, int endBlue, int endAlpha) {
        float heightHalf = triangleMult * width * 0.5f;
        float widthHalf = width * 0.5f;
        Matrix4f matrix4f = stack.getLast().getMatrix();
        IVertexBuilder buffer = renderTypeBuffer.getBuffer(RenderType.getLightning());
        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, -widthHalf, length, -heightHalf).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(matrix4f, widthHalf, length, -heightHalf).color(endRed, endGreen, endBlue, endAlpha).endVertex();

        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, widthHalf, length, -heightHalf).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(matrix4f, 0, length, heightHalf).color(endRed, endGreen, endBlue, endAlpha).endVertex();

        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, 0, length, heightHalf).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(matrix4f, -widthHalf, length, -heightHalf).color(endRed, endGreen, endBlue, endAlpha).endVertex();
    }

    public static void renderGradientBeams(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float length, float width, int ticks, float partialTicks, int amount, int endRed, int endGreen, int endBlue, int endAlpha) {
        matrixStack.push();
        random.setSeed(432L);
        for (int i = 0; i < amount; i++) {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F + partialTicks));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F + partialTicks));
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + partialTicks));
            float ticker = (ticks + partialTicks) / 200;
            int alpha = (int) (255.0F * (1.0F - ticker));
            renderGradientBeam(matrixStack, renderTypeBuffer, length, width, 255, 255, 255, alpha, endRed, endGreen, endBlue, endAlpha);
        }
        matrixStack.pop();
    }

    public static void renderGradientBeam(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float length, float width, int endRed, int endGreen, int endBlue, int endAlpha) {
        renderGradientBeam(matrixStack, renderTypeBuffer, length, width, 255, 255, 255, 255, endRed, endGreen, endBlue, endAlpha);
    }

    public static void renderGradientBeam(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float length, float width, int alpha, int endRed, int endGreen, int endBlue, int endAlpha) {
        renderGradientBeam(matrixStack, renderTypeBuffer, length, width, 255, 255, 255, alpha, endRed, endGreen, endBlue, endAlpha);
    }

    /**
     * Like {@link RenderUtils#renderGradientBeam3d} but 2d instead of cone shaped
     */
    public static void renderGradientBeam(MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float length, float width,
                                          int red, int green, int blue, int alpha, int endRed, int endGreen, int endBlue, int endAlpha) {
        float widthHalf = width * 0.5f;
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        IVertexBuilder buffer = renderTypeBuffer.getBuffer(RenderType.getLightning());
        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, -widthHalf, length, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(matrix4f, widthHalf, length, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();

        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, 0, 0, 0).color(red, green, blue, alpha).endVertex();
        buffer.pos(matrix4f, widthHalf, length, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(matrix4f, -widthHalf, length, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
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
}
