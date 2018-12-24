package com.flemmli97.tenshilib.common.events.handler;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import com.flemmli97.tenshilib.api.item.IDualWeaponRender;
import com.flemmli97.tenshilib.api.item.IExtendedWeapon;
import com.flemmli97.tenshilib.client.render.RenderUtils;
import com.flemmli97.tenshilib.common.config.ConfigHandler;
import com.flemmli97.tenshilib.common.events.LayerHeldItemEvent;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.PacketHit;
import com.flemmli97.tenshilib.common.network.PacketHit.HitType;
import com.flemmli97.tenshilib.proxy.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEvents {
	
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
				client.getItemRenderer().renderItemInFirstPerson(client.player, event.getPartialTicks(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), ((IDualWeaponRender)main.getItem()).offHandStack(client.player), ClientHandHandler.getInstance().equipProgress(EnumHand.OFF_HAND, event.getPartialTicks()));
			}
		}
	}
	
	@SubscribeEvent
	public void renderDualWeaponPlayer(LayerHeldItemEvent event)
	{
		if(event.getHand()==EnumHand.OFF_HAND)
		{
			ItemStack main = event.getEntity().getHeldItemMainhand();
			if(main.getItem() instanceof IDualWeaponRender)
			{
				event.setStack(((IDualWeaponRender)main.getItem()).offHandStack(event.getEntity()));
			}
		}
	}
	
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
	
	@SubscribeEvent
	public void swingEmpty(PlayerInteractEvent.LeftClickEmpty event)
	{
		ClientHandHandler.getInstance().resetSwing();
    	ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
		if(stack.getItem() instanceof IExtendedWeapon)
    	{
    		PacketHandler.sendToServer(new PacketHit(HitType.EXT));
    	}
		else if(stack.getItem() instanceof IAOEWeapon)
    	{
    		PacketHandler.sendToServer(new PacketHit(HitType.AOE));
    	}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTick(RenderWorldLastEvent event)
	{
		ClientHandHandler.getInstance().updateEquippedItem();
		if(((ClientProxy)TenshiLib.proxy).currentStructure!=null && ConfigHandler.showStructure)
		{
			for(StructureBoundingBox box : ((ClientProxy)TenshiLib.proxy).currentStructure.getBoxes())
				RenderUtils.renderBoundingBox(new AxisAlignedBB(box.maxX, box.maxY, box.maxZ, box.minX, box.minY, box.minZ), Minecraft.getMinecraft().player, event.getPartialTicks());
		}
	}
}
