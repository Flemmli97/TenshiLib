package com.flemmli97.tenshilib.common.events.handler;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.api.item.IDualWeaponRender;
import com.flemmli97.tenshilib.asm.ASMMethods;
import com.flemmli97.tenshilib.client.render.RenderUtils;
import com.flemmli97.tenshilib.common.config.ConfigHandler;
import com.flemmli97.tenshilib.common.events.LayerHeldItemEvent;
import com.flemmli97.tenshilib.proxy.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientEvents {
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderDualWeaponFirstPerson(RenderSpecificHandEvent event)
	{
		Minecraft client = Minecraft.getMinecraft();
		ItemStack main = client.player.getHeldItemMainhand();
		if(main.getItem() instanceof IDualWeaponRender)
		{
			if(event.getHand()==EnumHand.MAIN_HAND)
			{
				event.setCanceled(true);
				client.getItemRenderer().renderItemInFirstPerson(client.player, event.getPartialTicks(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), main, ClientHandHandler.getInstance().equipProgress(EnumHand.MAIN_HAND, event.getPartialTicks()));
			}
			else if(event.getHand()==EnumHand.OFF_HAND)
			{
				event.setCanceled(true);
				client.getItemRenderer().renderItemInFirstPerson(client.player, event.getPartialTicks(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), main, ClientHandHandler.getInstance().equipProgress(EnumHand.OFF_HAND, event.getPartialTicks()));
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderDualWeapon(LayerHeldItemEvent event)
	{
		if(event.getHand()==EnumHand.OFF_HAND)
		{
			Minecraft client = Minecraft.getMinecraft();
			ItemStack main = client.player.getHeldItemMainhand();
			if(main.getItem() ==Items.DIAMOND)//instanceof IDualWeaponRender)
			{
				event.setStack(main);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderDualWeaponPose(RenderLivingEvent.Pre<?> event)
	{
		if(event.getEntity() instanceof AbstractClientPlayer)
		{
			AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
			ItemStack heldMain = player.getHeldItemMainhand();
			boolean rightHand = player.getPrimaryHand() == EnumHandSide.RIGHT;
	        if (heldMain.getItem() instanceof IDualWeaponRender) 
	        {
	        	ModelPlayer model = (ModelPlayer) event.getRenderer().getMainModel();
                model.rightArmPose = rightHand?model.rightArmPose:model.leftArmPose;
                model.leftArmPose = rightHand?model.rightArmPose:model.leftArmPose;
	        }
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void swingEmpty(PlayerInteractEvent.LeftClickEmpty event)
	{
		ASMMethods.setSwing();
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase==Phase.END && Minecraft.getMinecraft().world!=null && Minecraft.getMinecraft().player!=null)
		{
			ClientHandHandler.getInstance().updateEquippedItem();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(RenderWorldLastEvent event)
	{
		if(((ClientProxy)TenshiLib.proxy).currentStructure!=null && ConfigHandler.showStructure)
		{
			for(StructureBoundingBox box : ((ClientProxy)TenshiLib.proxy).currentStructure.getBoxes())
				RenderUtils.renderBoundingBox(new AxisAlignedBB(box.maxX, box.maxY, box.maxZ, box.minX, box.minY, box.minZ), Minecraft.getMinecraft().player, event.getPartialTicks());
		}
	}
}
