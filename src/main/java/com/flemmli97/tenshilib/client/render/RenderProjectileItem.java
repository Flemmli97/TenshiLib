package com.flemmli97.tenshilib.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public abstract class RenderProjectileItem<T extends Entity> extends Render<T>{

    public RenderProjectileItem(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    @Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTick) {
		GlStateManager.pushMatrix();
        GlStateManager.translate(x, y+0.2, z);
        GlStateManager.enableRescaleNormal();
        switch(this.getRenderType(entity))
        {
			case NORMAL:
				GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
	            GlStateManager.rotate((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
	            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				break;
			case WEAPON:
				GlStateManager.rotate((entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick)+180, 0.0F, 1.0F, 0.0F);
	        	GlStateManager.rotate((entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick)+95, 1.0F, 0.0F, 0.0F);
	        	GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F);
				break; 
        }
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(this.getRenderItemStack(entity), TransformType.THIRD_PERSON_RIGHT_HAND);
        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        if (!this.renderOutlines)
        {
            this.renderName(entity, x, y, z);
        }
    }
   
	@Override
	protected ResourceLocation getEntityTexture(T entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
	
	public abstract ItemStack getRenderItemStack(T entity);
	
	/**
	 * weapons get rotated so e.g. a swords tip points towards the travel direction
	 */
	public abstract RenderType getRenderType(T entity);
	
	public enum RenderType
	{
		NORMAL,
		WEAPON;
	}
}
