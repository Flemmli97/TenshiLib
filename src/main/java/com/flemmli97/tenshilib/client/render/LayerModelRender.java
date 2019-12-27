package com.flemmli97.tenshilib.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class LayerModelRender<T extends EntityLivingBase> implements LayerRenderer<T>
{
    protected final RenderLivingBase<T> livingEntityRenderer;
    private final ModelBase model;
    private final ModelRenderer parentPart;

    public LayerModelRender(RenderLivingBase<T> livingEntityRendererIn, ModelBase model, ModelRenderer parentPart)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
        this.model=model;
        this.parentPart=parentPart;
    }

    @Override
    public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
    	if (entity instanceof EntityPlayer && (((EntityPlayer)entity).isUser() && this.livingEntityRenderer.getRenderManager().renderViewEntity != entity))
    		return;
    	if(entity.isInvisible())
    		return;
    	GlStateManager.pushMatrix();

        if (this.livingEntityRenderer.getMainModel().isChild)
        {
            GlStateManager.translate(0.0F, 0.75F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
        }
        if (entity.isSneaking())
        {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
        this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        this.parentPart.postRender(scale);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }
}
