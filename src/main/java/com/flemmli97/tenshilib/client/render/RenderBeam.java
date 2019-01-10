package com.flemmli97.tenshilib.client.render;

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

	private float width, length;
	
	public RenderBeam(RenderManager renderManager, float width, float segmentLength) {
		super(renderManager);
		this.width=width;
		this.length=segmentLength;
	}

	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		double distSq = entity.hitVec().squareDistanceTo(entity.startVec());
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
        RenderHelper.disableStandardItemLighting();
        float w = width/2f;
        GlStateManager.translate(x, y, z+w);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks + 90, 1.0F, 0.0F, 0.0F);
        
        //DO STUFF
        if(distSq<length)
        {
        	renderPart(distSq);
        }
        else
        {
        	int max = (int) (distSq/length);
        }
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableCull();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	private static void renderPart(double length)
	{
		for(int i = 0; i < 12; i++)		
		{
			//Face
		}
		/*Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();	
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(-xSize, 0D, -ySize).tex(0, 0).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(-xSize, 0D, ySize).tex(0, 1).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(xSize, 0D, ySize).tex(1, 1).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(xSize, 0D, -ySize).tex(1, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();*/
	}
}
