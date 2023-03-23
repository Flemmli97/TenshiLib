package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public abstract class RenderProjectileModel<T extends Entity> extends EntityRenderer<T> {

    protected final EntityModel<T> model;

    public RenderProjectileModel(EntityRendererProvider.Context ctx, EntityModel<T> model) {
        super(ctx);
        this.model = model;
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        stack.pushPose();
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + this.yawOffset();
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + this.pitchOffset();
        float partialLivingTicks = entity.tickCount + partialTicks;

        this.translate(entity, stack, pitch, yaw, partialTicks);

        this.model.prepareMobModel(entity, 0, 0, partialTicks);
        this.model.setupAnim(entity, 0, 0, partialLivingTicks, yaw, pitch);

        VertexConsumer ivertexbuilder = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(stack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.popPose();
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
    }

    public void translate(T entity, PoseStack stack, float pitch, float yaw, float partialTicks) {
        stack.mulPose(Axis.YP.rotationDegrees(180 + yaw));
        stack.mulPose(Axis.XP.rotationDegrees(pitch));
    }

    public float yawOffset() {
        return 0;
    }

    public float pitchOffset() {
        return 0;
    }
}
