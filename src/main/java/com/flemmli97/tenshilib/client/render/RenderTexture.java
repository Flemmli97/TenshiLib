package com.flemmli97.tenshilib.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

public abstract class RenderTexture<T extends Entity> extends Render<T>{

	private float xSize, ySize;
	private int red=255;
	private int green=255;
	private int blue=255;
	private int alpha=255;
	
	public RenderTexture(RenderManager renderManager, float xSize, float ySize) {
		super(renderManager);
		this.xSize=xSize;
		this.ySize=ySize;
	}
	
	public void setColor(int hexColor)
	{
		this.setColor(hexColor >> 16 & 255, hexColor >> 8 & 255, hexColor >> 0 & 255, hexColor >> 24 & 255);
	}
	
	public void setColor(int red, int green, int blue, int alpha)
	{
		this.red = red;
		this.blue = blue;
		this.green = green;
		this.alpha = alpha;
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (!this.renderOutlines)
        {
        	float yaw=entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks+180;
        	float pitch=entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            if(this.facePlayer())
            {
            	yaw=-this.renderManager.playerViewY+180;
                pitch=(this.renderManager.options.thirdPersonView == 2 ? 1 : -1) * this.renderManager.playerViewX;
            }
            
            RenderUtils.renderTexture(this.renderManager, this.getEntityTexture(entity), x, y, z, 
            		this.xSize, this.ySize, this.red, this.blue, this.green, this.alpha, yaw+this.yawOffset(),
            pitch+this.pitchOffset(), this.currentAnimation(entity), this.animationFrames());
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

	public boolean facePlayer() 
	{
		return true;
	}
	
	public float yawOffset()
	{
		return 0;
	}
	
	public float pitchOffset()
	{
		return 0;
	}
	
	public int animationFrames()
	{
		return 1;
	}
	
	public int currentAnimation(T entity)
	{
		return entity.ticksExisted%this.animationFrames()+1;
	}
}
