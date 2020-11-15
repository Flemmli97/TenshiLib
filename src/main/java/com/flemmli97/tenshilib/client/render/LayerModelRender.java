package com.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class LayerModelRender<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {

    private final EntityModel<T> model;
    private final LivingRenderer<T, M> renderer;
    private final ResourceLocation texture;

    public LayerModelRender(LivingRenderer<T, M> renderer, EntityModel<T> model, ResourceLocation texture) {
        super(renderer);
        this.model = model;
        this.renderer = renderer;
        this.texture = texture;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible())
            return;
        matrixStack.push();

        if (this.getEntityModel().isChild) {
            matrixStack.translate(0.0F, 0.75F, 0.0F);
            matrixStack.scale(0.5F, 0.5F, 0.5F);
        }
        if (entity.isSneaking()) {
            matrixStack.translate(0.0F, 0.2F, 0.0F);
        }
        this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        Minecraft mc = Minecraft.getInstance();
        boolean flag = !entity.isInvisible();
        boolean flag1 = !flag && !entity.isInvisibleToPlayer(mc.player);
        boolean flag2 = mc.hasOutline(entity);
        RenderType rendertype = this.getRenderType(entity, flag, flag1, flag2);
        if (rendertype != null) {
            IVertexBuilder ivertexbuilder = buffer.getBuffer(rendertype);
            int i = LivingRenderer.getOverlay(entity, 0);
            this.model.render(matrixStack, ivertexbuilder, packedLightIn, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }
        matrixStack.pop();
    }

    protected RenderType getRenderType(T entity, boolean invis, boolean canSee, boolean glowing) {
        ResourceLocation resourcelocation = this.texture;
        if (canSee) {
            return RenderType.getItemEntityTranslucentCull(resourcelocation);
        } else if (invis) {
            return this.model.getLayer(resourcelocation);
        } else {
            return glowing ? RenderType.getOutline(resourcelocation) : null;
        }
    }
}
