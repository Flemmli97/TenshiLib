package io.github.flemmli97.tenshilib.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

public class EntityStartTrackEvent {

    /**
     * Fabrics is at head and at that point the client doesn't know about the entity yet...
     */
    public static final Event<EntityTrackingEvents.StartTracking> START_TRACKING = EventFactory.createArrayBacked(EntityTrackingEvents.StartTracking.class, callbacks -> (trackedEntity, player) -> {
        for (EntityTrackingEvents.StartTracking callback : callbacks) {
            callback.onStartTracking(trackedEntity, player);
        }
    });
}
