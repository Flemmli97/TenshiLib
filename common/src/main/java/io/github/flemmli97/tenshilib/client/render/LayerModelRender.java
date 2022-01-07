package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class LayerModelRender<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    protected final EntityModel<T> model;
    protected final LivingEntityRenderer<T, M> renderer;
    protected final ResourceLocation texture;

    public LayerModelRender(LivingEntityRenderer<T, M> renderer, EntityModel<T> model, ResourceLocation texture) {
        super(renderer);
        this.model = model;
        this.renderer = renderer;
        this.texture = texture;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible())
            return;
        matrixStack.pushPose();

        if (this.getParentModel().young) {
            matrixStack.translate(0.0F, 0.75F, 0.0F);
            matrixStack.scale(0.5F, 0.5F, 0.5F);
        }
        if (entity.isShiftKeyDown()) {
            matrixStack.translate(0.0F, 0.2F, 0.0F);
        }
        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        Minecraft mc = Minecraft.getInstance();
        boolean flag = !entity.isInvisible();
        boolean flag1 = !flag && !entity.isInvisibleTo(mc.player);
        boolean flag2 = mc.shouldEntityAppearGlowing(entity);
        RenderType rendertype = this.getRenderType(entity, flag, flag1, flag2);
        if (rendertype != null) {
            VertexConsumer ivertexbuilder = buffer.getBuffer(rendertype);
            int i = LivingEntityRenderer.getOverlayCoords(entity, 0);
            this.model.renderToBuffer(matrixStack, ivertexbuilder, packedLightIn, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
        }
        matrixStack.popPose();
    }

    protected RenderType getRenderType(T entity, boolean invis, boolean canSee, boolean glowing) {
        ResourceLocation resourcelocation = this.texture;
        if (canSee) {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        } else if (invis) {
            return this.model.renderType(resourcelocation);
        } else {
            return glowing ? RenderType.outline(resourcelocation) : null;
        }
    }
}
