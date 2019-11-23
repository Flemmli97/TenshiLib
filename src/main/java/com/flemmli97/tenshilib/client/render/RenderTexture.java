package com.flemmli97.tenshilib.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

public abstract class RenderTexture<T extends Entity> extends Render<T>{

	public final float xSize, ySize;
	private int red=255;
	private int green=255;
	private int blue=255;
	private int alpha=255;
	public final int rows, columns,length;
	public final double uLength, vLength;
	public RenderTexture(RenderManager renderManager, float xSize, float ySize, int rows, int columns) {
		super(renderManager);
		this.xSize=xSize;
		this.ySize=ySize;
		this.rows=rows;
		this.columns=columns;
		this.length=rows*columns;
		this.uLength=1D/columns;
		this.vLength=1D/rows;
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
            double[] uvOffset = this.uvOffset(entity.ticksExisted);
            RenderUtils.renderTexture(this.renderManager, this.getEntityTexture(entity), x, y, z, 
            		this.xSize, this.ySize, this.red, this.blue, this.green, this.alpha, yaw+this.yawOffset(),
            pitch+this.pitchOffset(), uvOffset[0], uvOffset[1], this.uLength, this.vLength);
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
	
	public double[] uvOffset(int timer) {
	    int frame = timer % this.length;
        return new double[] {(frame % columns) * this.uLength, (frame / columns) * this.vLength};
	}
}
