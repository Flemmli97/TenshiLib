package com.flemmli97.tenshilib.asm;

import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import com.flemmli97.tenshilib.api.item.IDualWeaponRender;
import com.flemmli97.tenshilib.api.item.IExtendedWeapon;
import com.flemmli97.tenshilib.common.config.ConfigUtils;
import com.flemmli97.tenshilib.common.events.LayerHeldItemEvent;
import com.flemmli97.tenshilib.common.events.ModelRotationEvent;
import com.flemmli97.tenshilib.common.events.PathFindInitEvent;
import com.flemmli97.tenshilib.common.events.handler.ClientHandHandler;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.PacketHit;
import com.flemmli97.tenshilib.common.network.PacketHit.HitType;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class ASMMethods {
    
    public static void attackEntityClient(PlayerControllerMP controller, EntityPlayer player, Entity entity)
    {
    	ClientHandHandler.getInstance().resetSwing();
    	if(player.getHeldItemMainhand().getItem() instanceof IExtendedWeapon)
    	{
    		PacketHandler.sendToServer(new PacketHit(HitType.EXT));
    	}
    	else if(player.getHeldItemMainhand().getItem() instanceof IAOEWeapon)
    	{
    		PacketHandler.sendToServer(new PacketHit(HitType.AOE));
    	}
    	else
    		controller.attackEntity(player, entity);
    }
    
    public static void swingArm(EntityPlayer player, EnumHand originHand) {
    	if(player.getHeldItemMainhand().getItem() instanceof IDualWeaponRender)
    		player.swingArm(ClientHandHandler.getInstance().currentHand());
    	else
    		player.swingArm(originHand);
    }
    
    public static void modelEvent(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
			float scaleFactor, Entity entityIn, RenderLivingBase<?> render)
    {
    	MinecraftForge.EVENT_BUS.post(new ModelRotationEvent(render, scaleFactor, scaleFactor, scaleFactor, scaleFactor, scaleFactor, scaleFactor, entityIn));
    }
    
	public static PathFinder pathFinderInitEvent(PathNavigate navigator, PathFinder defaultFinder) {
		PathFindInitEvent event = new PathFindInitEvent(navigator, defaultFinder);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getPathFinder();
   }
	
	public static ItemStack layerHeldItemEvent(EntityLivingBase entity, ItemStack stack, EnumHand hand) 
	{
		LayerHeldItemEvent event = new LayerHeldItemEvent(entity, stack, hand);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getStack();
	}
	
	public static void configLoad(Configuration config, String category, Class<?> clss, Object obj)
	{
		ConfigUtils.load(config, category, clss, obj);
	}
}
