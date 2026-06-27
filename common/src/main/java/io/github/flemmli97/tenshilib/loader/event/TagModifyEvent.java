package io.github.flemmli97.tenshilib.loader.event;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.github.flemmli97.tenshilib.loader.LoaderInitializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Event to allow dynamic modifications of tags
 */
public interface TagModifyEvent {

    TagModifyEvent INSTANCE = LoaderInitializer.getImplInstance(TagModifyEvent.class,
            "io.github.flemmli97.tenshilib.fabric.events.TagModifyEventImpl",
            "io.github.flemmli97.tenshilib.neoforge.events.TagModifyEventImpl");

    /**
     * Register a handler for this event without depending on platform loader
     */
    <T> void registerListener(ResourceKey<? extends Registry<T>> key, Consumer<TagLoadingHolder<T>> handler);

    default <T> void trigger(ResourceKey<? extends Registry<T>> registry, RegistryAccess registryAccess, Map<ResourceLocation, Collection<Holder<T>>> original) {
        TagLoadingHolder<T> holder = new TagLoadingHolder<>(registry, registryAccess, original);
        this.trigger(holder);
        holder.added().forEach((tag, value) -> original.compute(tag, (k, v) -> {
            if (v == null) {
                return ImmutableSet.copyOf(value);
            }
            ImmutableSet.Builder<Holder<T>> builder = ImmutableSet.builder();
            builder.addAll(v);
            builder.addAll(value);
            return builder.build();
        }));
        holder.filter().forEach((tag, filters) -> {
            Collection<Holder<T>> holders = original.get(tag);
            if (holders != null) {
                ImmutableSet.Builder<Holder<T>> builder = ImmutableSet.builder();
                holders.stream().filter(h -> filters.stream().noneMatch(p -> p.test(h)))
                        .forEach(builder::add);
                original.put(tag, builder.build());
            }
        });
    }

    <T> void trigger(TagLoadingHolder<T> holder);

    class TagLoadingHolder<T> {

        private final ResourceKey<? extends Registry<T>> key;
        private final RegistryAccess registryAccess;
        private final Map<ResourceLocation, Collection<Holder<T>>> original;

        private final Map<ResourceLocation, Collection<Holder<T>>> add = Maps.newHashMap();
        private final Map<ResourceLocation, Collection<Predicate<Holder<T>>>> filter = Maps.newHashMap();

        public TagLoadingHolder(ResourceKey<? extends Registry<T>> key, RegistryAccess registryAccess, Map<ResourceLocation, Collection<Holder<T>>> original) {
            this.key = key;
            this.registryAccess = registryAccess;
            this.original = ImmutableMap.copyOf(original);
        }

        /**
         * @return The current registry this TagLoader is for
         */
        public ResourceKey<? extends Registry<T>> key() {
            return this.key;
        }

        public RegistryAccess registryAccess() {
            return this.registryAccess;
        }

        public void add(TagKey<T> tag, Holder<T> value) {
            this.add(tag.location(), value);
        }

        /**
         * Add an entry to the current tag. Will create a new tag if not present
         */
        public void add(ResourceLocation tag, Holder<T> value) {
            this.add.computeIfAbsent(tag, k -> new ArrayList<>()).add(value);
        }

        public void addFilter(TagKey<T> tag, Predicate<Holder<T>> value) {
            this.addFilter(tag.location(), value);
        }

        /**
         * Adds a filter to remove current tags
         * Filters matching the given one will be removed
         */
        public void addFilter(ResourceLocation tag, Predicate<Holder<T>> filter) {
            this.filter.computeIfAbsent(tag, k -> new ArrayList<>()).add(filter);
        }

        @Nullable
        public Collection<Holder<T>> get(ResourceLocation tag) {
            return this.original.get(tag);
        }

        public Map<ResourceLocation, Collection<Holder<T>>> added() {
            return ImmutableMap.copyOf(this.add);
        }

        public Map<ResourceLocation, Collection<Predicate<Holder<T>>>> filter() {
            return ImmutableMap.copyOf(this.filter);
        }
    }
}
