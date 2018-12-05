package com.flemmli97.tenshilib.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUtils {

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
}
