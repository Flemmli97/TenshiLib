package com.flemmli97.tenshilib.client.render;

import com.flemmli97.tenshilib.mixin.WorldRenderAccessor;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
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

    public static void renderBlockOutline(MatrixStack matrixStack, IRenderTypeBuffer buffer, PlayerEntity player, BlockPos pos, float partialTicks) {
        renderBlockOutline(matrixStack, buffer, player, pos, partialTicks, 0, 0, 0, 1, false);
    }

    public static void renderBlockOutline(MatrixStack matrixStack, IRenderTypeBuffer buffer, PlayerEntity player, BlockPos pos, float partialTicks, float red, float green, float blue, float alpha,
            boolean ignoreDepth) {
        BlockState state = player.world.getBlockState(pos);
        IVertexBuilder build;
        if(ignoreDepth)
            build = buffer.getBuffer(MoreRenderTypes.LINE_NODEPTH);
        else
            build = buffer.getBuffer(RenderType.getLines());
        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
        Vector3d rpos = renderInfo.getProjectedView();
        WorldRenderAccessor.drawShapeOutline(matrixStack, build, state.getShape(player.world, pos, ISelectionContext.forEntity(player)),
                rpos.x,rpos.y,rpos.z, red, green, blue, alpha);
    }

    public static void renderAreaAround(MatrixStack matrixStack, IRenderTypeBuffer buffer, PlayerEntity player, BlockPos pos, float partialTicks, float radius) {
        RenderUtils.renderBoundingBox(matrixStack, buffer, new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius).offset(pos.down()), player, partialTicks);
    }

    public static void renderAreaAround(MatrixStack matrixStack, IRenderTypeBuffer buffer, PlayerEntity player, BlockPos pos, float partialTicks, float radius, float red, float green, float blue,
            float alpha, boolean ignoreDepth) {
        RenderUtils.renderBoundingBox(matrixStack, buffer, new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius).offset(pos.down()), player, partialTicks, red, green, blue,
                alpha, ignoreDepth);
    }

    public static void renderBoundingBox(MatrixStack matrixStack, IRenderTypeBuffer buffer, AxisAlignedBB aabb, PlayerEntity player, float partialTicks) {
        RenderUtils.renderBoundingBox(matrixStack, buffer, aabb, player, partialTicks, 1, 0.5F, 0.5F, 1, false);
    }

    public static void renderBoundingBox(MatrixStack matrixStack, IRenderTypeBuffer buffer, AxisAlignedBB aabb, PlayerEntity player, float partialTicks, float red, float green, float blue, float alpha,
            boolean ignoreDepth) {
        double playerX = player.lastTickPosX + (player.getX() - player.lastTickPosX) * partialTicks;
        double playerY = player.lastTickPosY + (player.getY() - player.lastTickPosY) * partialTicks;
        double playerZ = player.lastTickPosZ + (player.getZ() - player.lastTickPosZ) * partialTicks;
        IVertexBuilder build;
        if(ignoreDepth)
            build = buffer.getBuffer(MoreRenderTypes.LINE_NODEPTH);
        else
            build = buffer.getBuffer(RenderType.getLines());
        WorldRenderer.drawBox(matrixStack, build, aabb.grow(0.0020000000949949026D).offset(-playerX, -playerY, -playerZ), red, green, blue, alpha);
    }

    public static void applyYawPitch(MatrixStack stack, float yaw, float pitch){
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
        builder.vertex(matrix4f, xSize, ySize, 0).color(red, green, blue, alpha).texture(u+uLength, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, xSize, -ySize, 0).color(red, green, blue, alpha).texture(u+uLength, v+vLength).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(mat3f, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -xSize, -ySize, 0).color(red, green, blue, alpha).texture(u, v+vLength).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(mat3f, 0, 0, 1).endVertex();
    }

    public static void renderTexture(MatrixStack matrixStack, IVertexBuilder builder, float xSize, float ySize, int hexColor, int light) {
        int red = hexColor >> 16 & 255;
        int green = hexColor >> 8 & 255;
        int blue = hexColor & 255;
        int alpha = hexColor >> 24 & 255;
        renderTexture(matrixStack, builder, xSize, ySize, red, green, blue, alpha, light);
    }
}
