package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

/**
 * Render an entity using a texture in the shape of an arrow entity
 */
public abstract class RenderCrossedTextureEntity<T extends Entity> extends RenderTexture<T> {

    public RenderCrossedTextureEntity(EntityRendererProvider.Context ctx, float xSize, float ySize, int rows, int columns) {
        super(ctx, xSize, ySize, rows, columns);
    }

    @Override
    public void doRender(T entity, float partialTicks, PoseStack stack, MultiBufferSource buffer) {
        stack.pushPose();
        stack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        for (int j = 0; j < 2; ++j) {
            stack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            RenderUtils.renderTexture(stack, buffer.getBuffer(this.getRenderType(entity, this.getTextureLocation(entity))), this.xSize, this.ySize, this.textureBuilder);
        }
        stack.popPose();
    }

    @Override
    public boolean facePlayer() {
        return false;
    }

    @Override
    public float yawOffset() {
        return -90.0F;
    }
}
