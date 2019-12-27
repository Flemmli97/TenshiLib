package com.flemmli97.tenshilib.common.entity;

import javax.annotation.Nullable;

import com.flemmli97.tenshilib.common.network.PacketAnimatedEntity;
import com.flemmli97.tenshilib.common.network.PacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public interface IAnimated {

	@Nullable
	public AnimatedAction getAnimation();
	
	public void setAnimation(AnimatedAction anim);
	
	public AnimatedAction[] getAnimations();
	
	public default void tickAnimation()
	{
		if(this.getAnimation()!=null && this.getAnimation().tick())
			this.setAnimation(null);
	}
	
	public static <T extends Entity & IAnimated> void sentToClient(T entity)
	{
		if(!entity.world.isRemote)
		{
			WorldServer world = (WorldServer) entity.world;
			for(EntityPlayer player : world.getEntityTracker().getTrackingPlayers(entity))
				PacketHandler.sendTo(new PacketAnimatedEntity(entity, entity.getAnimation()), (EntityPlayerMP) player);
		}
	}
}
