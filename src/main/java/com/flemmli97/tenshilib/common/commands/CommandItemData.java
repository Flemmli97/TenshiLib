package com.flemmli97.tenshilib.common.commands;


import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandItemData implements ICommand{

	private final List<String> aliases = new ArrayList<String>();

	public CommandItemData()
	{
		this.aliases.add("itemData");
	}
	
	@Override
	public String getName() {
		return "itemData";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.itemdata.usage";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length<1 || (args[0].equals("modify") && args.length<2))
        {
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);
        }
		if(sender.getCommandSenderEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
			ItemStack stack = args.length>1?player.inventory.getStackInSlot(MathHelper.clamp(Integer.parseInt(args[1])-1, 0, 9)):player.getHeldItemMainhand();
			if(stack.isEmpty())
			{
				return;
			}
			NBTTagCompound stackCompound = stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();

			if(args[0].equals("view"))
			{
				player.sendMessage(new TextComponentString(TextFormatting.GOLD+stackCompound.toString()));
			}
			if(args[0].equals("modify"))
			{
				NBTTagCompound fromCommand;
				try
                {
					fromCommand = JsonToNBT.getTagFromJson(CommandBase.buildString(args, args.length>1?2:1));
                }
                catch (NBTException nbtexception)
                {
                    throw new CommandException("commands.itemdata.tagError", new Object[] {nbtexception.getMessage()});
                }
				stackCompound.merge(fromCommand);
				stack.setTagCompound(stackCompound);
                CommandBase.notifyCommandListener(sender, this, "commands.itemdata.success", new Object[] {stackCompound.toString()});
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(2, this.getName());
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if(args.length==1)
			return Lists.newArrayList("view", "modify");
		return Lists.newArrayList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}
}
