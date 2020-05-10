package com.flemmli97.tenshilib.common.commands;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class CommandItemData implements ICommand {

	private final List<String> aliases = Lists.newArrayList();

	public CommandItemData() {
		this.aliases.add("tenshilib:itemdata");
	}

	@Override
	public String getName() {
		return "tenshilib:itemdata";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.itemdata.usage";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1){
			throw new WrongUsageException(this.getUsage(sender));
		}
		if(args[0].equals("modify")){
			if(!sender.canUseCommand(2, this.getName()))
				throw new CommandException("commands.generic.permission");
			if(args.length < 2)
				throw new WrongUsageException(this.getUsage(sender));
		}
		if(sender.getCommandSenderEntity() instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
			int slot = -1;
			if(args.length > 1){
				try{
					slot = Integer.parseInt(args[1]);
				}catch(NumberFormatException e){
				}
			}
			ItemStack stack = slot != -1 ? player.inventory.getStackInSlot(MathHelper.clamp(slot - 1, 0, 9)) : player.getHeldItemMainhand();
			if(stack.isEmpty()){
				return;
			}
			NBTTagCompound stackCompound = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
			if(args[0].equals("view")){
				if(slot != -1 && args.length > 2 || slot == -1 && args.length > 1){
					String s = slot != -1 ? args[2] : args[1];
					NBTBase nbt = stackCompound.getTag(s);
					if(nbt != null)
						player.sendMessage(new TextComponentString(TextFormatting.GOLD + nbt.toString()));
					else
						throw new CommandException("command.itemdata.noSuchTag", s);
				}else
					player.sendMessage(new TextComponentString(TextFormatting.GOLD + stackCompound.toString()));
			}
			if(args[0].equals("modify")){
				if(sender.canUseCommand(2, this.getName())){
					NBTTagCompound fromCommand;
					try{
						fromCommand = JsonToNBT.getTagFromJson(CommandBase.buildString(args, slot != -1 ? 2 : 1));
					}catch(NBTException nbtexception){
						throw new CommandException("command.itemdata.tagError", nbtexception.getMessage());
					}
					stackCompound.merge(fromCommand);
					stack.setTagCompound(stackCompound);
					CommandBase.notifyCommandListener(sender, this, "command.itemdata.success", stackCompound.toString());
				}else{
					throw new CommandException("commands.generic.permission");
				}
			}
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if(args.length == 1)
			if(sender.canUseCommand(2, this.getName()))
				return Lists.newArrayList("view", "modify");
			else
				return Lists.newArrayList("view");
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
