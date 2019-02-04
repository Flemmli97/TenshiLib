package com.flemmli97.tenshilib.client.render;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.flemmli97.tenshilib.api.entity.IBeamEntity;

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

	private float width;
	private int red=255;
	private int green=255;
	private int blue=255;
	private int alpha=255;
	
	public RenderBeam(RenderManager renderManager, float width) {
		super(renderManager);
		this.width=width;
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
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		double dist = entity.hitVec().distanceTo(entity.startVec());
		double width = this.width * (Math.sin(Math.sqrt(entity.ticksExisted/(float)entity.livingTickMax())*Math.PI))*2;
		GlStateManager.pushMatrix();        
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
    	GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        //GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 1/255f);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.depthMask(false);
        
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks+90, 0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
    	double startLength = 0;
    	if(this.startTexture(entity)!=null)
    	{
    		startLength = Math.min(this.startTexture(entity).getRight(), dist);
            this.renderManager.renderEngine.bindTexture(this.startTexture(entity).getLeft());

    		for(int i = 0; i < 2; i++)
            {
                GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
                this.renderBeam(width, startLength, 0);
            }
    	}
    	double length = dist-startLength;
    	if(this.endTexture(entity)!=null)
        {
    		double endLength = Math.min(this.endTexture(entity).getRight(), length);
            this.renderManager.renderEngine.bindTexture(this.endTexture(entity).getLeft());
        	length-=endLength;
            for(int i = 0; i < 2; i++)
            {
                GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
                this.renderBeam(width, endLength, startLength+length);
            }
        }
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entity));
        for(int i = 0; i < 2; i++)
        {
            GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F);
            this.renderBeam(width, length, startLength);
        }
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	private void renderBeam(double width, double length, double offset)
	{
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();	
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(offset, width, 0).tex(0, 0).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(offset+length, width, 0).tex(1, 0).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(offset+length, -width, 0).tex(1, 1).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(offset, -width, 0).tex(0, 1).color(this.red, this.green, this.blue, this.alpha).endVertex();
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

}
