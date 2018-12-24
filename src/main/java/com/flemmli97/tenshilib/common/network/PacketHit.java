package com.flemmli97.tenshilib.common.network;

import java.util.List;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import com.flemmli97.tenshilib.api.item.IExtendedWeapon;
import com.flemmli97.tenshilib.common.events.AOEAttackEvent;
import com.flemmli97.tenshilib.common.world.RayTraceUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketHit  implements IMessage{

	public HitType type;
	
	public PacketHit(){}
	
	public PacketHit(HitType type)
	{
		this.type = type;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.type=HitType.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.type.ordinal());

	}
	
	public static class Handler implements IMessageHandler<PacketHit, IMessage> {

        @Override
        public IMessage onMessage(PacketHit msg, MessageContext ctx) {
    		EntityPlayer player =  TenshiLib.proxy.getPlayerEntity(ctx);
			ItemStack stack = player.getHeldItemMainhand();
			if(msg.type==HitType.EXT && stack.getItem() instanceof IExtendedWeapon)
			{
				IExtendedWeapon item = (IExtendedWeapon) stack.getItem();
				RayTraceResult res = RayTraceUtils.calculateEntityFromLook(player, item.getRange());
				if(res!=null && res.entityHit!=null)
					player.attackTargetEntityWithCurrentItem(res.entityHit);
			}
			if(msg.type==HitType.AOE && stack.getItem() instanceof IAOEWeapon)
			{
				IAOEWeapon item = (IAOEWeapon) stack.getItem();
				List<EntityLivingBase> list = RayTraceUtils.getEntities(player, item.getRange(), item.getFOV());
				if(MinecraftForge.EVENT_BUS.post(new AOEAttackEvent(player, list)))
					return null;
				for(int i = 0; i < list.size(); i++)
					AOEAttackEvent.attackTargetEntityWithCurrentItem(player, list.get(i), i==list.size()-1);
			}
            return null;
        }
    }

	public static enum HitType
	{
		EXT,
		AOE;
	}
}
