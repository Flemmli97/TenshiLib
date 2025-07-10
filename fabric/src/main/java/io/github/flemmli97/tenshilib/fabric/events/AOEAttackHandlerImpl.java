package io.github.flemmli97.tenshilib.fabric.events;

import io.github.flemmli97.tenshilib.loader.event.AOEAttackHandler;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AOEAttackHandlerImpl implements AOEAttackHandler {

    /**
     * Preferred: Use the interface instance at {@link AOEAttackHandler#INSTANCE}
     */
    public static final Event<AOEAttackEvent> EVENT = EventFactory.createArrayBacked(AOEAttackEvent.class,
            (listeners) -> (player, stack, list) -> {
                for (AOEAttackEvent event : listeners) {
                    if (event.preventAttack(player, stack, list))
                        return true;
                }
                return false;
            }
    );

    @Override
    public void registerAOEEventHandler(AOEAttackEvent handler) {
        EVENT.register(handler);
    }

    @Override
    public boolean trigger(Player player, ItemStack stack, List<Entity> list) {
        return EVENT.invoker().preventAttack(player, stack, list);
    }
}