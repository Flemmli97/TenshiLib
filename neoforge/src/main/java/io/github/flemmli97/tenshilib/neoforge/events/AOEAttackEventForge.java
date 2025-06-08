package io.github.flemmli97.tenshilib.neoforge.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

public class AOEAttackEventForge extends PlayerEvent implements ICancellableEvent {

    private final List<Entity> list;

    public final ItemStack usedItem;

    public AOEAttackEventForge(Player player, ItemStack stack, List<Entity> attackList) {
        super(player);
        this.usedItem = stack;
        this.list = attackList;
    }

    public List<Entity> attackList() {
        return this.list;
    }
}