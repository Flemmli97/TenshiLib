package com.flemmli97.tenshilib.common.network;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.entity.AnimatedAction;
import com.flemmli97.tenshilib.common.entity.IAnimated;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketAnimatedEntity implements IMessage{

	private int entityID, animID;

	public PacketAnimatedEntity(){}
	
	public PacketAnimatedEntity(Entity entity, AnimatedAction anim)
	{
		if(!(entity instanceof IAnimated))
			return;
		this.entityID=entity.getEntityId();
		if(anim==null)
			this.animID=-2;	
		else if(anim==AnimatedAction.vanillaAttack)
			this.animID=-1;
		else
		{
			int i = 0;
			for(AnimatedAction a : ((IAnimated) entity).getAnimations())
			{
				if(a.getID()==anim.getID())
					break;
				i++;
			}
			this.animID=i;
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityID=buf.readInt();
		this.animID=buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityID);
		buf.writeInt(this.animID);
	}
	
	public static class Handler implements IMessageHandler<PacketAnimatedEntity, IMessage> {

        @Override
        public IMessage onMessage(PacketAnimatedEntity msg, MessageContext ctx) {
    		Entity e = TenshiLib.proxy.getPlayerEntity(ctx).world.getEntityByID(msg.entityID);
    		if(e instanceof IAnimated)
    		{
    			IAnimated anim = (IAnimated) e;
    			anim.setAnimation(msg.animID==-2?null:msg.animID==-1?AnimatedAction.vanillaAttack:anim.getAnimations()[msg.animID]);
    		}
            return null;
        }
    }

}
