package io.github.flemmli97.tenshilib.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public class EntityTickEvent {

    public static final Event<EntityTicker> ON_ENTITY_TICK_PRE = EventFactory.createArrayBacked(EntityTicker.class, callbacks -> entity -> {
        for (EntityTicker callback : callbacks) {
            callback.accept(entity);
        }
    });

    public static final Event<EntityTicker> ON_ENTITY_TICK_POST = EventFactory.createArrayBacked(EntityTicker.class, callbacks -> entity -> {
        for (EntityTicker callback : callbacks) {
            callback.accept(entity);
        }
    });

    public interface EntityTicker extends Consumer<Entity> {

    }
}
