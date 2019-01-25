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
	private int hexColor;
	public RenderBeam(RenderManager renderManager, float width, int hexColor) {
		super(renderManager);
		this.width=width;
		this.hexColor=hexColor;
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		double dist = entity.hitVec().distanceTo(entity.startVec());
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks+90, 0.0F, -1.0F, 0.0F);
        GlStateManager.rotate(-(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 0.0F, 0.0F, 1.0F);
        //TODO: improve this part
        GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entity));
        this.renderBeam(this.width, dist);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableCull();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
	private void renderBeam(double width, double length)
	{
		int red = this.hexColor >> 16 & 255;
        int blue = this.hexColor >> 8 & 255;
        int green = this.hexColor >> 0 & 255;
        int alpha = this.hexColor >> 24 & 255;
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();	
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(0, -width, 0).tex(0, 0).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(0, width, 0).tex(0, 1).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(length, width, 0).tex(1, 1).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(length, -width, 0).tex(1, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
	}
}
