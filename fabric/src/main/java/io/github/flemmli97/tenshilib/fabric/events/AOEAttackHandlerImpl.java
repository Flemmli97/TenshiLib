package io.github.flemmli97.tenshilib.fabric.events;

import io.github.flemmli97.tenshilib.loader.event.AOEAttackHandler;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class AOEAttackHandlerImpl implements AOEAttackHandler {

    public static final Event<AOEAttackEvent> ATTACK = EventFactory.createArrayBacked(AOEAttackEvent.class,
            (listeners) -> (player, stack, list) -> {
                for (AOEAttackEvent event : listeners) {
                    if (event.preventAttack(player, stack, list))
                        return false;
                }
                return true;
            }
    );

    @Override
    public void registerAOEEventHandler(AOEAttackEvent handler) {
        ATTACK.register(handler);
    }
}