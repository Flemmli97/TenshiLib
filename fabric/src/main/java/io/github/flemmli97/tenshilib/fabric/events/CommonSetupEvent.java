package io.github.flemmli97.tenshilib.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class CommonSetupEvent {

    public static final Event<QueuedWorkListener> COMMON_SETUP = EventFactory.createArrayBacked(QueuedWorkListener.class,
            (listeners) -> handler -> {
                for (QueuedWorkListener event : listeners) {
                    event.handle(handler);
                }
            }
    );

    public interface QueuedWorkHandler {

        void enqueue(String modid, Runnable runnable);
    }

    public interface QueuedWorkListener {

        void handle(QueuedWorkHandler handler);
    }
}
