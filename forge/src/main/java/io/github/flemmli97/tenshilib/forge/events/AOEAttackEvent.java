package io.github.flemmli97.tenshilib.forge.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

@Cancelable
public class AOEAttackEvent extends PlayerEvent {

    private final List<Entity> list;

    public final ItemStack usedItem;

    public AOEAttackEvent(Player player, ItemStack stack, List<Entity> attackList) {
        super(player);
        this.usedItem = stack;
        this.list = attackList;
    }

    public List<Entity> attackList() {
        return this.list;
    }
}