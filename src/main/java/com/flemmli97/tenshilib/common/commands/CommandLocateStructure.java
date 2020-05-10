package com.flemmli97.tenshilib.common.commands;

import com.flemmli97.tenshilib.common.world.structure.StructureBase;
import com.flemmli97.tenshilib.common.world.structure.StructureGenerator;
import com.flemmli97.tenshilib.common.world.structure.StructureMap;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class CommandLocateStructure implements ICommand {

    private final List<String> aliases = Lists.newArrayList();

    public CommandLocateStructure() {
        this.aliases.add("structures");
    }

    @Override
    public String getName() {
        return "structures";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.structures.usage";
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
        String id = args[0];
        StructureBase base = StructureMap.get(sender.getEntityWorld()).getNearestStructure(new ResourceLocation(id), sender.getPosition(),
                sender.getEntityWorld());
        String translation = "structures." + id;
        if(base != null){
            sender.sendMessage(
                    new TextComponentTranslation("commands.locate.success", translation, base.getPos().getX(), base.getPos().getZ()));
        }else{
            throw new CommandException("commands.locate.failure", translation);
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(2, this.getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if(args.length == 1){
            List<String> list = Lists.newArrayList();
            for(ResourceLocation res : StructureGenerator.allRegisteredStructures())
                list.add(res.toString());
            list.sort(null);
            return list;
        }
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
