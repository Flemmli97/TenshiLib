package io.github.flemmli97.tenshilib.fabric.loader.events;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface CommonSetupEvent {

    /**
     * This event runs AFTER {@link ModInitializer#onInitialize()} to mimic the neoforge equivalent of FMLCommonSetupEvent
     */
    Event<QueuedWorkListener> EVENT = EventFactory.createArrayBacked(QueuedWorkListener.class,
            (listeners) -> handler -> {
                for (QueuedWorkListener event : listeners) {
                    event.handle(handler);
                }
            }
    );

    interface QueuedWorkHandler {

        void enqueue(String modid, Runnable runnable);
    }

    interface QueuedWorkListener {

        void handle(QueuedWorkHandler handler);
    }
}
