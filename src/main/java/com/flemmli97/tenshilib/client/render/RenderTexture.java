package com.flemmli97.tenshilib.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

public abstract class RenderTexture<T extends Entity> extends Render<T>{

	private float xSize, ySize;
	private int hexColor;
	
	public RenderTexture(RenderManager renderManager, int hexColor, float xSize, float ySize) {
		super(renderManager);
		this.xSize=xSize;
		this.ySize=ySize;
		this.hexColor=hexColor;
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (!this.renderOutlines)
        {
            RenderUtils.renderTexture(this.renderManager, this.getEntityTexture(entity), x, y, z, 
            		this.xSize, this.ySize, this.hexColor, entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks,
            entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }
}
