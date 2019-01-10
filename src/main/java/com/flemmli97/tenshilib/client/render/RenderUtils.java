package com.flemmli97.tenshilib.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUtils {
	
	public static final int defaultColor = 0xFFFFFFFF;

	public static void renderBlockOutline(EntityPlayer player, BlockPos pos, float partialTicks)
    {
		AxisAlignedBB aabb = player.world.getBlockState(pos).getSelectedBoundingBox(player.world, pos);
		if(aabb!=null)
		{
			RenderUtils.renderBoundingBox(aabb, player, partialTicks);
		}
    }
	
    public static void renderBlockOutline(EntityPlayer player, BlockPos pos, float partialTicks, float red, float green, float blue, float alpha, boolean ignoreDepth)
    {
		AxisAlignedBB aabb = player.world.getBlockState(pos).getSelectedBoundingBox(player.world, pos);
		if(aabb!=null)
		{
			RenderUtils.renderBoundingBox(aabb, player, partialTicks, red, green, blue, alpha, ignoreDepth);
		}	
    }
    
    public static void renderAreaAround(EntityPlayer player,  BlockPos pos, float partialTicks, float radius)
    {
    	RenderUtils.renderBoundingBox(new AxisAlignedBB(0,0,0,1,1,1).grow(radius).offset(pos.down()), player, partialTicks);
    }

    public static void renderAreaAround(EntityPlayer player,  BlockPos pos, float partialTicks, float radius, float red, float green, float blue, float alpha, boolean ignoreDepth)
    {
    	RenderUtils.renderBoundingBox(new AxisAlignedBB(0,0,0,1,1,1).grow(radius).offset(pos.down()), player, partialTicks, red, green, blue, alpha, ignoreDepth);
    }
    
    public static void renderBoundingBox(AxisAlignedBB aabb, EntityPlayer player, float partialTicks)
    {
    	RenderUtils.renderBoundingBox(aabb, player, partialTicks, 1, 0.5F, 0.5F, 1, false);
    }
    
    public static void renderBoundingBox(AxisAlignedBB aabb, EntityPlayer player, float partialTicks, float red, float green, float blue, float alpha, boolean ignoreDepth)
    {
    	double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(2);
		if(ignoreDepth)
			GlStateManager.disableDepth();
		RenderGlobal.drawSelectionBoundingBox(aabb.grow(0.0020000000949949026D).offset(-playerX, -playerY, -playerZ), 1, 0.5F, 0.5F, 1);
		if(ignoreDepth)
			GlStateManager.enableDepth();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
    }
    
    public static void renderTexture(RenderManager renderManager, ResourceLocation texture, double x, double y, double z, float xSize, float ySize, int hexColor, float yawRot, float pitchRot)
    {
    	renderManager.renderEngine.bindTexture(texture);
    	GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 1/255f);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.translate(x, y+0.2, z);
        GlStateManager.rotate(yawRot, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-pitchRot, 1.0F, 0.0F, 0.0F);
        xSize = xSize/2f;
        ySize = ySize/2f;
        int red = hexColor >> 16 & 255;
        int blue = hexColor >> 8 & 255;
        int green = hexColor >> 0 & 255;
        int alpha = hexColor >> 24 & 255;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();	
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(-xSize, -ySize, 0).tex(0, 0).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(-xSize, ySize, 0).tex(0, 1).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(xSize, ySize, 0).tex(1, 1).color(red, green, blue, alpha).endVertex();
        vertexbuffer.pos(xSize, -ySize, 0).tex(1, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();	
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
