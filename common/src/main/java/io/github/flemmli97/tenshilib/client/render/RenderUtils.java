package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.github.flemmli97.tenshilib.common.utils.math.OrientedBoundingBox;
import net.minecraft.Util;
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
import java.util.function.Function;

public class RenderUtils {

    public static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static final float TRIANGLE_MULT = (float) (Math.sqrt(3.0D) / 2.0D);
    private static final Random RANDOM = new Random(432);

    private static final Function<RenderType, RenderType> TRIG_STRIP = Util.memoize((wrapped) ->
            new RenderType("rendertype_trig_wrapped_" + wrapped.toString(), wrapped.format(), VertexFormat.Mode.TRIANGLE_STRIP, wrapped.bufferSize(),
                    wrapped.affectsCrumbling(), wrapped.sortOnUpload(), wrapped::setupRenderState, wrapped::clearRenderState) {
            });

    public static float getPartialTicks(Entity entity) {
        return Minecraft.getInstance().getTimer()
                .getGameTimeDeltaPartialTick(!entity.level().tickRateManager().isEntityFrozen(entity));
    }

    public static void renderBlockOutline(PoseStack poseStack, MultiBufferSource buffer, Player player, BlockPos pos, float partialTick, boolean drawImmediately) {
        renderBlockOutline(poseStack, buffer, player, pos, partialTick, 0, 0, 0, 1, drawImmediately);
    }

    /**
     * Renders the block shape at the given position
     *
     * @param drawImmediately Most of the time this should be true.
     *                        Else it will get drawn next frame and the position will be offset by player movement
     */
    public static void renderBlockOutline(PoseStack poseStack, MultiBufferSource buffer, Player player, BlockPos pos, float partialTick, float red, float green, float blue, float alpha,
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

    public static void renderGradientBeams3d(PoseStack stack, MultiBufferSource renderTypeBuffer, float length, float width, int ticks, float partialTick, float rotationPerTick, int amount, BeamBuilder builder) {
        stack.pushPose();
        RANDOM.setSeed(432L);
        for (int i = 0; i < amount; i++) {
            float ticker = ticks + partialTick;
            stack.mulPose(Axis.XP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            stack.mulPose(Axis.YP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            stack.mulPose(Axis.ZP.rotationDegrees(RANDOM.nextFloat() * 360.0F + ticker * rotationPerTick));
            renderGradientBeam3d(stack, renderTypeBuffer, length, width, builder);
        }
        stack.popPose();
    }

    /**
     * Renders a gradient triangular cone-shaped beam similar to the beams displayed during the enderdragons death
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

    public static void renderGradientBeams(PoseStack poseStack, MultiBufferSource renderTypeBuffer, float length, float width, int ticks, float partialTick, float rotationPerTick, int amount, BeamBuilder builder) {
        poseStack.pushPose();
        RANDOM.setSeed(432L);
        for (int i = 0; i < amount; i++) {
            float ticker = ticks + partialTick;
            poseStack.mulPose(Axis.XP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(RANDOM.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(RANDOM.nextFloat() * 360.0F + ticker * rotationPerTick));
            renderGradientBeam(poseStack, renderTypeBuffer, length, width, builder);
        }
        poseStack.popPose();
    }

    /**
     * Like {@link RenderUtils#renderGradientBeam3d} but 2d instead of cone-shaped
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
     * Renders a sphere. Can handle both quads and triangle strips rendertypes
     *
     * @param precision       How many points should be used along the axis. Higher creates more spherical shapes
     * @param drawImmediately If true draws the content immediately to the buffer
     */
    public static void renderSphere(MultiBufferSource buffer, RenderType renderType, PoseStack stack,
                                    float red, float green, float blue, float alpha,
                                    float radius, int precision, int light, boolean drawImmediately,
                                    float u0, float v0, float u1, float v1) {
        if (renderType.mode() != VertexFormat.Mode.TRIANGLE_STRIP) {
            renderType = TRIG_STRIP.apply(renderType);
        }
        VertexConsumer consumer = buffer.getBuffer(renderType);
        renderSphere(consumer, stack, red, green, blue, alpha, radius, precision, light, u0, v0, u1, v1);
        if (drawImmediately && buffer instanceof MultiBufferSource.BufferSource source)
            source.endBatch();
    }

    /**
     * Renders a sphere. Requires a triangle stripe rendertype being used
     *
     * @param precision How many points should be used along the axis. Higher creates more spherical shapes
     */
    public static void renderSphere(VertexConsumer consumer, PoseStack stack,
                                    float red, float green, float blue, float alpha,
                                    float radius, int precision, int light,
                                    float u0, float v0, float u1, float v1) {
        stack.pushPose();
        stack.mulPose(Axis.XN.rotationDegrees(90));
        PoseStack.Pose pose = stack.last();
        float step = Mth.PI / precision;
        float uL = u1 - u0;
        float vL = v1 - v0;
        for (float t = 0; t < precision; t++) {
            for (float p = 0; p <= precision * 2; p++) {
                float theta = t * step;
                float phi = p * step;
                float thetaNext = theta + step;
                float x = radius * Mth.sin(theta) * Mth.cos(phi);
                float y = radius * Mth.sin(theta) * Mth.sin(phi);
                float z = radius * Mth.cos(theta);
                float u = p / (precision * 2) * uL;
                // Degenerate vertices to break trig strips
                if (t == 0 && p == 0) {
                    consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha).setUv(u0 + u, v0 + vL * t / precision).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
                }
                consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha).setUv(u0 + u, v0 + vL * t / precision).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
                x = radius * Mth.sin(thetaNext) * Mth.cos(phi);
                y = radius * Mth.sin(thetaNext) * Mth.sin(phi);
                z = radius * Mth.cos(thetaNext);
                float v = v0 + vL * (t + 1) / precision;
                consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha).setUv(u0 + u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
                // Degenerate vertices to break trig strips
                if (t == precision - 1 && p == precision * 2) {
                    consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha).setUv(u0 + u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
                }
            }
        }
        stack.popPose();
    }

    /**
     * Improved version of {@link InventoryScreen#renderEntityInInventory}.
     * Automatically scales the entity if it's too big
     *
     * @param x     Top left x position
     * @param y     Top left y positon
     * @param sizeX X Size of the render area
     * @param sizeY Y Size of the render area
     */
    public static void renderScaledEntityGui(GuiGraphics guiGraphics, float x, float y, float sizeX, float sizeY, float scale,
                                             float yOffset, float mouseX, float mouseY, LivingEntity entity) {
        float maxSizeWidth = sizeX / scale;
        float maxSizeHeight = sizeY / scale;
        float scaleMult = 1;
        if (entity.getBbWidth() > maxSizeWidth) {
            scaleMult = maxSizeWidth / entity.getBbWidth();
        }
        if (entity.getBbHeight() > maxSizeHeight) {
            scaleMult = Math.min(scaleMult, maxSizeHeight / entity.getBbHeight());
        }
        renderEntityMouseNoClip(guiGraphics,
                x, y, x + sizeX, y + sizeY,
                scale * scaleMult, yOffset, mouseX, mouseY, entity);
    }

    private static void renderEntityMouseNoClip(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float scale, float yOffset, float mouseX, float mouseY, LivingEntity entity) {
        float xM = (x1 + x2) / 2.0f;
        float yM = (y1 + y2) / 2.0f;
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
