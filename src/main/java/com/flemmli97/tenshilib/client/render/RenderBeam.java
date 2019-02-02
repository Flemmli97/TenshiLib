package com.flemmli97.tenshilib.client.render;

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
		GlStateManager.pushMatrix();        
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 1/255f);
        RenderHelper.disableStandardItemLighting();
        
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks+90, 0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
        //TODO: improve this part
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entity));
        this.renderBeam(this.width, dist);
        
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	private void renderBeam(double width, double length)
	{
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();	
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(0, width, 0).tex(0, 0).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(length, width, 0).tex(1, 0).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(length, -width, 0).tex(1, 1).color(this.red, this.green, this.blue, this.alpha).endVertex();
        vertexbuffer.pos(0, -width, 0).tex(0, 1).color(this.red, this.green, this.blue, this.alpha).endVertex();
        tessellator.draw();
	}
}
