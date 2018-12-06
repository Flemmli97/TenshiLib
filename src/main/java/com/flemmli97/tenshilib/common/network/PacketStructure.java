package com.flemmli97.tenshilib.common.network;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.world.StructureBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketStructure  implements IMessage{

	public StructureBase structure;	
	public PacketStructure(){}
	
	public PacketStructure(StructureBase structure)
	{
		this.structure = structure;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		try
		{
			NBTTagCompound compound = ByteBufUtils.readTag(buf);
			if(compound!=null)
				this.structure=new StructureBase(compound);
		}
		catch(IndexOutOfBoundsException e) {}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if(this.structure!=null)
		ByteBufUtils.writeTag(buf, this.structure.writeToNBT(new NBTTagCompound()));
	}
	
	public static class Handler implements IMessageHandler<PacketStructure, IMessage> {

        @Override
        public IMessage onMessage(PacketStructure msg, MessageContext ctx) {
        	TenshiLib.proxy.setStructureToRender(msg.structure);	
            return null;
        }
    }
}