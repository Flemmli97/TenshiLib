package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.flemmli97.tenshilib.common.utils.math.OrientedBoundingBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

public class RenderUtils {

    public static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static final float TRIANGLE_MULT = (float) (Math.sqrt(3.0D) / 2.0D);
    private static final Random RANDOM = new Random(432);

    public static float getPartialTicks() {
        return Minecraft.getInstance().getTimer()
                .getGameTimeDeltaTicks();
    }

    public static float getPartialTicks(Entity entity) {
        return Minecraft.getInstance().getTimer()
                .getGameTimeDeltaPartialTick(!entity.level().tickRateManager().isEntityFrozen(entity));
    }

    public static void renderBlockOutline(PoseStack poseStack, MultiBufferSource buffer, Player player, BlockPos pos, float partialTicks, boolean drawImmediately) {
        renderBlockOutline(poseStack, buffer, player, pos, partialTicks, 0, 0, 0, 1, drawImmediately);
    }

    /**
     * Renders the block shape at the given position
     *
     * @param drawImmediately Most of the time this should be true.
     *                        Else it will get drawn next frame and the position will be offset by player movement
     */
    public static void renderBlockOutline(PoseStack poseStack, MultiBufferSource buffer, Player player, BlockPos pos, float partialTicks, float red, float green, float blue, float alpha,
                                          boolean drawImmediately) {
        BlockState state = player.level().getBlockState(pos);
        RenderType renderType = RenderType.lines();
        Vec3 vec = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        renderShape(poseStack, buffer.getBuffer(renderType), state.getShape(player.level(), pos, CollisionContext.of(player)),
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
            consumer.addVertex(pose.pose(), (float) (k + x), (float) (l + y), (float) (m + z)).setColor(red, green, blue, alpha).setNormal(pose, q /= t, r /= t, s /= t);
            consumer.addVertex(pose.pose(), (float) (n + x), (float) (o + y), (float) (p + z)).setColor(red, green, blue, alpha).setNormal(pose, q, r, s);
        });
    }

    public static void renderAreaAround(PoseStack poseStack, MultiBufferSource buffer, BlockPos pos, float radius, boolean drawImmediately) {
        renderBoundingBox(poseStack, buffer, new AABB(0, 0, 0, 1, 1, 1).inflate(radius).move(pos.below()), drawImmediately);
    }

    public static void renderAreaAround(PoseStack poseStack, MultiBufferSource buffer, BlockPos pos, float radius, float red, float green, float blue,
                                        float alpha, boolean ignoreDepth, boolean drawImmediately) {
        renderBoundingBox(poseStack, buffer, new AABB(0, 0, 0, 1, 1, 1).inflate(radius).move(pos.below()), red, green, blue,
                alpha, drawImmediately);
    }

    public static void renderBoundingBox(PoseStack poseStack, MultiBufferSource buffer, AABB aabb, boolean drawImmediately) {
        RenderUtils.renderBoundingBox(poseStack, buffer, aabb, 1, 0.5F, 0.5F, 1, drawImmediately);
    }

    /**
     * Renders the given bounding box similiar to entity hit boxes
     *
     * @param drawImmediately Most of the time this should be true.
     *                        Else it will get drawn next frame and the position will be offset by player movement
     */
    public static void renderBoundingBox(PoseStack poseStack, MultiBufferSource buffer, AABB aabb, float red, float green, float blue, float alpha,
                                         boolean drawImmediately) {
        Vec3 vec = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        RenderType renderType = RenderType.lines();
        LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(renderType), aabb.inflate(0.002).move(-vec.x, -vec.y, -vec.z), red, green, blue, alpha);
        if (drawImmediately && buffer instanceof MultiBufferSource.BufferSource)
            ((MultiBufferSource.BufferSource) buffer).endBatch();
    }

    /**
     * Renders the given oriented bounding box
     *
     * @param drawImmediately If true draws the content immediately to the buffer
     */
    public static void renderOBB(PoseStack stack, MultiBufferSource buffer, OrientedBoundingBox obb, float red, float green, float blue, float alpha,
                                 boolean drawImmediately) {
        Vec3 vec = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        stack.pushPose();
        stack.translate(-vec.x, -vec.y, -vec.z);
        RenderType renderType = RenderType.lines();
        VertexConsumer consumer = buffer.getBuffer(renderType);
        PoseStack.Pose pose = stack.last();
        for (int b = 0; b < 4; b++) {
            Vec3 first = obb.getVertices()[b];
            Vec3 second = obb.getVertices()[(b + 1) % 4];
            consumer.addVertex(pose, (float) first.x(), (float) first.y(), (float) first.z()).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
            consumer.addVertex(pose, (float) second.x(), (float) second.y(), (float) second.z()).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);

            // Vertical lines
            Vec3 top = obb.getVertices()[4 + b];
            consumer.addVertex(pose, (float) first.x(), (float) first.y(), (float) first.z()).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
            consumer.addVertex(pose, (float) top.x(), (float) top.y(), (float) top.z()).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        }

        for (int b = 4; b < 8; b++) {
            Vec3 first = obb.getVertices()[b];
            Vec3 second = obb.getVertices()[4 + (b + 1) % 4];
            consumer.addVertex(pose, (float) first.x(), (float) first.y(), (float) first.z()).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
            consumer.addVertex(pose, (float) second.x(), (float) second.y(), (float) second.z()).setColor(red, green, blue, alpha).setNormal(pose, 1.0F, 0.0F, 0.0F);
        }
        if (drawImmediately && buffer instanceof MultiBufferSource.BufferSource)
            ((MultiBufferSource.BufferSource) buffer).endBatch();
        stack.popPose();
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
        PoseStack.Pose pose = stack.last();
        builder.addVertex(matrix4f, -xSize, ySize, 0).setColor(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).setUv(textureBuilder.u, textureBuilder.v).setOverlay(textureBuilder.overlay).setLight(textureBuilder.light).setNormal(pose, 0, 0, 1);
        builder.addVertex(matrix4f, xSize, ySize, 0).setColor(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).setUv(textureBuilder.u + textureBuilder.uLength, textureBuilder.v).setOverlay(textureBuilder.overlay).setLight(textureBuilder.light).setNormal(pose, 0, 0, 1);
        builder.addVertex(matrix4f, xSize, -ySize, 0).setColor(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).setUv(textureBuilder.u + textureBuilder.uLength, textureBuilder.v + textureBuilder.vLength).setOverlay(textureBuilder.overlay).setLight(textureBuilder.light).setNormal(pose, 0, 0, 1);
        builder.addVertex(matrix4f, -xSize, -ySize, 0).setColor(textureBuilder.red, textureBuilder.green, textureBuilder.blue, textureBuilder.alpha).setUv(textureBuilder.u, textureBuilder.v + textureBuilder.vLength).setOverlay(textureBuilder.overlay).setLight(textureBuilder.light).setNormal(pose, 0, 0, 1);
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
        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, -widthHalf, length, -heightHalf).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);
        buffer.addVertex(matrix4f, widthHalf, length, -heightHalf).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);

        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, widthHalf, length, -heightHalf).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);
        buffer.addVertex(matrix4f, 0, length, heightHalf).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);

        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, 0, length, heightHalf).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);
        buffer.addVertex(matrix4f, -widthHalf, length, -heightHalf).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);
    }

    public static void renderGradientBeams(PoseStack poseStack, MultiBufferSource renderTypeBuffer, float length, float width, int ticks, float partialTicks, float rotationPerTick, int amount, BeamBuilder builder) {
        poseStack.pushPose();
        RANDOM.setSeed(432L);
        for (int i = 0; i < amount; i++) {
            float ticker = ticks + partialTicks;
            poseStack.mulPose(Axis.XP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(RANDOM.nextFloat() * 360.0F + ticker * rotationPerTick));
            renderGradientBeam(poseStack, renderTypeBuffer, length, width, builder);
        }
        poseStack.popPose();
    }

    /**
     * Like {@link RenderUtils#renderGradientBeam3d} but 2d instead of cone shaped
     *
     * @param builder Structure containing rendering info like color etc. Is mutable so cache an instance of it.
     */
    public static void renderGradientBeam(PoseStack poseStack, MultiBufferSource renderTypeBuffer, float length, float width, BeamBuilder builder) {
        float widthHalf = width * 0.5f;
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer buffer = renderTypeBuffer.getBuffer(builder.renderType);
        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, -widthHalf, length, 0).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);
        buffer.addVertex(matrix4f, widthHalf, length, 0).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);

        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, 0, 0, 0).setColor(builder.red, builder.green, builder.blue, builder.alpha);
        buffer.addVertex(matrix4f, widthHalf, length, 0).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);
        buffer.addVertex(matrix4f, -widthHalf, length, 0).setColor(builder.endRed, builder.endGreen, builder.endBlue, builder.endAlpha);
    }

    /**
     * Improved version of {@link InventoryScreen#renderEntityInInventory}.
     * Automatically scales the entity if its too big
     *
     * @param x         Top left x position
     * @param y         Top left y positon
     * @param maxWidth  Width in blocks. E.g. width of 1 = entity that are 1 block wide
     * @param maxHeight Height in blocks
     */
    // TODO better impl
    public static void renderScaledEntityGui(GuiGraphics guiGraphics, int x, int y, int scale, float maxWidth, float maxHeight,
                                             float yOffset, float mouseX, float mouseY, LivingEntity entity) {
        int sizeX = (int) (maxWidth * scale);
        int sizeY = (int) (maxHeight * scale);
        float scaleMult = 1;
        if (entity.getBbWidth() > maxWidth) {
            scaleMult = maxWidth / entity.getBbWidth();
        }
        if (entity.getBbHeight() > maxHeight) {
            scaleMult = Math.min(scaleMult, maxHeight / entity.getBbHeight());
        }
        renderEntityMouseNoClip(guiGraphics,
                x, y, x + sizeX, y + sizeY,
                (int) (scale * scaleMult), yOffset, mouseX, mouseY, entity);
    }

    private static void renderEntityMouseNoClip(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, float scale, float yOffset, float mouseX, float mouseY, LivingEntity entity) {
        float xM = (float) (x1 + x2) / 2.0f;
        float yM = (float) (y1 + y2) / 2.0f;
        float yRot = (float) Math.atan((xM - mouseX) / 40.0f);
        float xRot = (float) Math.atan((yM - mouseY) / 40.0f);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(xRot * 20.0f * ((float) Math.PI / 180));
        quaternionf.mul(quaternionf2);
        float preYBody = entity.yBodyRot;
        float preYRot = entity.getYRot();
        float preXRot = entity.getXRot();
        float preYHead0 = entity.yHeadRotO;
        float preYHead = entity.yHeadRot;
        entity.yBodyRot = 180.0f + yRot * 20.0f;
        entity.setYRot(180.0f + yRot * 40.0f);
        entity.setXRot(-xRot * 20.0f);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        float entityScale = entity.getScale();
        Vector3f offset = new Vector3f(0.0f, entity.getBbHeight() / 2.0f + yOffset * entityScale, 0.0f);
        scale = scale / entityScale;
        InventoryScreen.renderEntityInInventory(guiGraphics, xM, yM, scale, offset, quaternionf, quaternionf2, entity);
        entity.yBodyRot = preYBody;
        entity.setYRot(preYRot);
        entity.setXRot(preXRot);
        entity.yHeadRotO = preYHead0;
        entity.yHeadRot = preYHead;
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
