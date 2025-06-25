package io.github.flemmli97.tenshilib.loader.event;

import io.github.flemmli97.tenshilib.loader.LoaderInitializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface AOEAttackHandler {

    AOEAttackHandler INSTANCE = LoaderInitializer.getImplInstance(AOEAttackHandler.class,
            "io.github.flemmli97.tenshilib.fabric.events.AOEAttackHandlerImpl",
            "io.github.flemmli97.tenshilib.neoforge.events.AOEAttackHandlerImpl");

    /**
     * Register a handler for this event without depending on platform loader
     */
    void registerAOEEventHandler(AOEAttackEvent handler);

    boolean trigger(Player player, ItemStack stack, List<Entity> list);

    interface AOEAttackEvent {

        /**
         * @param player     The corresponding player
         * @param stack      The item used to attack
         * @param attackList List of entities to attack
         * @return false to cancel the attack
         */
        boolean preventAttack(Player player, ItemStack stack, List<Entity> attackList);
    }
}
