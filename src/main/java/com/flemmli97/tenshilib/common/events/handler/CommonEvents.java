package com.flemmli97.tenshilib.common.events.handler;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.PacketStructure;
import com.flemmli97.tenshilib.common.world.structure.StructureMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class CommonEvents {
	
	private boolean prev;
	@SubscribeEvent
	public void tick(PlayerTickEvent event)
	{
		if(!event.player.world.isRemote)
		{
			boolean now = event.player.world.getGameRules().getBoolean("showBoundingBox");
			if(now)
				StructureMap.get(event.player.world).current(event.player, prev!=now);
			else
				PacketHandler.sendTo(new PacketStructure(null), (EntityPlayerMP) event.player);
			this.prev=event.player.world.getGameRules().getBoolean("showBoundingBox");
		}
	}

	@SubscribeEvent
	public void config(OnConfigChangedEvent event)
	{
		if(event.getModID().equals(TenshiLib.MODID))
			ConfigManager.sync(event.getModID(), Type.INSTANCE);
	}
}
