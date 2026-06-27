package io.github.flemmli97.tenshilib.neoforge.events;

import io.github.flemmli97.tenshilib.loader.event.TagModifyEvent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

public class TagModifyEventImpl implements TagModifyEvent {

    @SuppressWarnings("unchecked")
    @Override
    public <T> void registerListener(ResourceKey<? extends Registry<T>> key, Consumer<TagLoadingHolder<T>> handler) {
        Consumer<TagHolderEvent> cons = event -> {
            if (event.holder.key().location().equals(key.location()))
                handler.accept((TagLoadingHolder<T>) event.holder);
        };
        NeoForge.EVENT_BUS.addListener(cons);
    }

    @Override
    public <T> void trigger(TagLoadingHolder<T> holder) {
        NeoForge.EVENT_BUS.post(new TagHolderEvent(holder));
    }

    public static class TagHolderEvent extends Event {

        private final TagLoadingHolder<?> holder;

        public TagHolderEvent(TagLoadingHolder<?> holder) {
            this.holder = holder;
        }

        public TagLoadingHolder<?> holder() {
            return this.holder;
        }
    }
}
