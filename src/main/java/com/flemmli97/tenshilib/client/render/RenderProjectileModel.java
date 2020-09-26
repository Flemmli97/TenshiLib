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

public abstract class RenderProjectileModel<T extends Entity> extends EntityRenderer<T> {

    private EntityModel<T> model;

    public RenderProjectileModel(EntityRendererManager renderManagerIn, EntityModel<T> model) {
        super(renderManagerIn);
        this.model = model;
    }

    @Override
    public void render(T entity, float rotation, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight) {
        stack.push();
        stack.scale(-1.0F, -1.0F, 1.0F);
        float f = MathHelper.lerpAngle(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
        float f1 = MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch);
        IVertexBuilder ivertexbuilder = buffer.getBuffer(this.model.getLayer(this.getEntityTexture(entity)));
        //Setup rotation
        this.model.render(stack, ivertexbuilder, packedLight, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.pop();
        super.render(entity, rotation, partialTicks, stack, buffer, packedLight);
    }

    public float yawOffset() {
        return 0;
    }

    public float pitchOffset() {
        return 0;
    }
}
