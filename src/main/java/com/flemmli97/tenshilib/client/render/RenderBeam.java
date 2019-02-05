package com.flemmli97.tenshilib.client.render;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.flemmli97.tenshilib.api.entity.IBeamEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public abstract class RenderBeam<T extends Entity & IBeamEntity> extends Render<T>{

	
	protected final float radius;
	private int red=255;
	private int green=255;
	private int blue=255;
	private int alpha=255;
	
	public RenderBeam(RenderManager renderManager, float width) {
		super(renderManager);
		this.radius=width;
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
	
	public void setAlpha(int alpha)
	{
		this.alpha=alpha;
	}
	
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		entity.updateYawPitch();
		double dist = entity.hitVec().distanceTo(entity.startVec());
		double width = this.widthFunc(entity);
		GlStateManager.pushMatrix();        
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 1/255f);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.depthMask(false);
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks+90, 0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
        int animationFrame = this.currentAnimation(entity);
        double startLength = 0;
        int layer = entity.getShooter()==Minecraft.getMinecraft().player?1:2;
    	if(this.startTexture(entity)!=null)
    	{
    		startLength = Math.min(this.startTexture(entity).getRight(), dist);
            this.renderManager.renderEngine.bindTexture(this.startTexture(entity).getLeft());

    		for(int i = 0; i < layer; i++)
            {
                this.renderBeam(width, startLength, 0, animationFrame);
            }
    	}
    	double length = dist-startLength;
    	if(this.endTexture(entity)!=null)
        {
    		double endLength = Math.min(this.endTexture(entity).getRight(), length);
            this.renderManager.renderEngine.bindTexture(this.endTexture(entity).getLeft());
        	length-=endLength;
            for(int i = 0; i < layer; i++)
            {
                GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
                this.renderBeam(width, endLength, startLength+length, animationFrame);
            }
        }
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entity));
        double[] segments = this.segmentLength()==0?new double[] {length}:this.split(length);
        for(int i = 0; i < layer; i++)
        {
            GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
            for(int d = 0; d < segments.length; d++)
            this.renderBeam(width, segments[d], startLength+d*this.segmentLength(), animationFrame);
        }
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	private void renderBeam(double width, double length, double offset, int animationFrame)
	{
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();	
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(offset, width, 0).tex(0, Math.max(0, (animationFrame-1)*1/(float)this.animationFrames())).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(offset+length, width, 0).tex(1, Math.max(0, (animationFrame-1)*1/(float)this.animationFrames())).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(offset+length, -width, 0).tex(1, animationFrame*1/(float)this.animationFrames()).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(offset, -width, 0).tex(0, animationFrame*1/(float)this.animationFrames()).color(this.red, this.green, this.blue, this.alpha).endVertex();
        tessellator.draw();
	}
	
	/**
	 * Start texture of the beam. With size
	 */
	public abstract Pair<ResourceLocation,Integer> startTexture(T entity);
	
	/**
	 * End texture of the beam. With size
	 */
	public abstract Pair<ResourceLocation,Integer> endTexture(T entity);
	
	public double widthFunc(T entity)
	{
		return this.radius * (Math.sin(Math.sqrt(entity.ticksExisted/(float)entity.livingTickMax())*Math.PI))*2;
	}
	
	public double segmentLength()
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
	
	private double[] split(double length)
	{
		int arrL = (int) Math.ceil(length/this.segmentLength());
		double[] arr = new double[arrL];
		for(int i = 0; i < arr.length; i++)
		{
			arr[i] = Math.max(0, length-i*this.segmentLength());
		}
		return arr;
	}
}
