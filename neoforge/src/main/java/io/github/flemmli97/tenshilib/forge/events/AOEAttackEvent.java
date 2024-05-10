package io.github.flemmli97.tenshilib.forge.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

public class AOEAttackEvent extends PlayerEvent implements ICancellableEvent {

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