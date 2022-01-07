package io.github.flemmli97.tenshilib.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface AOEAttackEvent {

    /**
     * @param player     The corresponding player
     * @param stack      The item used to attack
     * @param attackList List of entities to attack
     * @return false to cancel the attack
     */
    boolean call(Player player, ItemStack stack, List<Entity> attackList);

    Event<AOEAttackEvent> ATTACK = EventFactory.createArrayBacked(AOEAttackEvent.class,
            (listeners) -> (player, stack, list) -> {
                for (AOEAttackEvent event : listeners) {
                    if (!event.call(player, stack, list))
                        return false;
                }
                return true;
            }
    );
}