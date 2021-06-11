package com.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public abstract class RenderProjectileModel<T extends Entity> extends EntityRenderer<T> {

    protected final EntityModel<T> model;

    public RenderProjectileModel(EntityRendererManager renderManagerIn, EntityModel<T> model) {
        super(renderManagerIn);
        this.model = model;
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight) {
        stack.push();
        float yaw = MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) + this.yawOffset();
        float pitch = MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch) + this.pitchOffset();
        float partialLivingTicks = entity.ticksExisted + partialTicks;

        this.translate(entity, stack, pitch, yaw, partialTicks);

        this.model.setLivingAnimations(entity, 0, 0, partialTicks);
        this.model.setRotationAngles(entity, 0, 0, partialLivingTicks, yaw, pitch);

        IVertexBuilder ivertexbuilder = buffer.getBuffer(this.model.getRenderType(this.getEntityTexture(entity)));
        this.model.render(stack, ivertexbuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.pop();
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
    }

    public void translate(T entity, MatrixStack stack, float pitch, float yaw, float partialTicks) {
        stack.rotate(Vector3f.YP.rotationDegrees(180 + yaw));
        stack.rotate(Vector3f.XP.rotationDegrees(pitch));
    }

    public float yawOffset() {
        return 0;
    }

    public float pitchOffset() {
        return 0;
    }
}
