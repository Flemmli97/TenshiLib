package com.flemmli97.tenshilib.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

public abstract class RenderProjectileModel<T extends Entity> extends Render<T>{

	private ModelBase model;

    public RenderProjectileModel(RenderManager renderManagerIn, ModelBase model)
    {
        super(renderManagerIn);
        this.model = model;
    }

    @Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTick) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick - this.yawOffset(), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick-this.pitchOffset(), 1.0F, 0.0F, 0.0F);
        GlStateManager.enableRescaleNormal();
    	this.bindEntityTexture(entity);
        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }
        this.model.render(entity, 0, 0, 0, 0, 0, 0.0625F);
        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();  
        super.doRender(entity, x, y, z, entityYaw, partialTick);
    }
    
    public float yawOffset() { return 0;}
    public float pitchOffset() { return 0;}
}
