package io.github.flemmli97.tenshilib.fabric.events;

import io.github.flemmli97.tenshilib.loader.event.TagModifyEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Consumer;

public class TagModifyEventImpl implements TagModifyEvent {

    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(Listener.class,
            listeners -> holder -> {
                for (Listener event : listeners) {
                    event.accept(holder);
                }
            }
    );

    @SuppressWarnings("unchecked")
    @Override
    public <T> void registerListener(ResourceKey<? extends Registry<T>> key, Consumer<TagLoadingHolder<T>> handler) {
        EVENT.register(holder -> {
            if (holder.key().location().equals(key.location()))
                handler.accept((TagLoadingHolder<T>) holder);
        });
    }

    @Override
    public <T> void trigger(TagLoadingHolder<T> holder) {
        EVENT.invoker().accept(holder);
    }

    public interface Listener extends Consumer<TagLoadingHolder<?>> {

    }
}