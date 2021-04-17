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

@OnlyIn(Dist.CLIENT)
public class RenderUtils {

    public static final int defaultColor = 0xFFFFFFFF;

    public static void renderBlockOutline(MatrixStack matrixStack, IRenderTypeBuffer buffer, PlayerEntity player, BlockPos pos, float partialTicks, boolean drawImmediately) {
        renderBlockOutline(matrixStack, buffer, player, pos, partialTicks, 0, 0, 0, 1, false, drawImmediately);
    }

    /**
     * Renders the block shape at the given position
     * @param ignoreDepth Doesnt work atm
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
                pos.getX()-vec.x, pos.getY()-vec.y, pos.getZ()-vec.z, red, green, blue, alpha);
        if(drawImmediately && buffer instanceof IRenderTypeBuffer.Impl)
            ((IRenderTypeBuffer.Impl) buffer).draw(renderType);
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
     * @param ignoreDepth Doesnt work atm
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
        WorldRenderer.drawBox(matrixStack, buffer.getBuffer(renderType), aabb.grow(0.002).offset(-vec.x, -vec.y, -vec.z), red, green, blue, alpha);
        if(drawImmediately && buffer instanceof IRenderTypeBuffer.Impl)
            ((IRenderTypeBuffer.Impl) buffer).draw();
    }

    public static void applyYawPitch(MatrixStack stack, float yaw, float pitch) {
        stack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(yaw));
        stack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(pitch));
    }

    public static void renderTexture(MatrixStack matrixStack, IVertexBuilder builder, float xSize, float ySize, int light) {
        renderTexture(matrixStack, builder, xSize, ySize, 255, 255, 255, 255, 0, 0, 1, 1, light);
    }

    public static void renderTexture(MatrixStack matrixStack, IVertexBuilder buffer, float xSize, float ySize, float red, float green, float blue, float alpha, int light) {
        renderTexture(matrixStack, buffer, xSize, ySize, (int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255), light);
    }

    public static void renderTexture(MatrixStack matrixStack, IVertexBuilder builder, float xSize, float ySize, int red, int green, int blue, int alpha, int light) {
        renderTexture(matrixStack, builder, xSize, ySize, red, green, blue, alpha, 0, 0, 1, 1, light);
    }

    public static void renderTexture(MatrixStack stack, IVertexBuilder builder, float xSize, float ySize, int red, int green, int blue, int alpha, float u, float v, float uLength, float vLength, int light) {
        xSize = xSize / 2f;
        ySize = ySize / 2f;
        Matrix4f matrix4f = stack.peek().getModel();
        Matrix3f mat3f = stack.peek().getNormal();
        builder.vertex(matrix4f, -xSize, ySize, 0).color(red, green, blue, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, xSize, ySize, 0).color(red, green, blue, alpha).texture(u + uLength, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, xSize, -ySize, 0).color(red, green, blue, alpha).texture(u + uLength, v + vLength).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -xSize, -ySize, 0).color(red, green, blue, alpha).texture(u, v + vLength).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(mat3f, 0, 0, 1).endVertex();
    }

    public static void renderTexture(MatrixStack matrixStack, IVertexBuilder builder, float xSize, float ySize, int hexColor, int light) {
        int red = hexColor >> 16 & 255;
        int green = hexColor >> 8 & 255;
        int blue = hexColor & 255;
        int alpha = hexColor >> 24 & 255;
        renderTexture(matrixStack, builder, xSize, ySize, red, green, blue, alpha, light);
    }
}
