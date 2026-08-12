package io.github.flemmli97.tenshilib.client.data;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Instance holder to easily reload the content during a resource reload
 */
public class ReloadableCache<T> {

    private final List<Consumer<T>> listeners = new ArrayList<>();

    private T cache;

    @ApiStatus.Internal
    public static <T> ReloadableCache<T> of(T value) {
        ReloadableCache<T> cache = new ReloadableCache<>();
        cache.update(value);
        return cache;
    }

    ReloadableCache<T> onChange(Consumer<T> consumer) {
        if (consumer != null) {
            this.listeners.add(consumer);
            if (this.cache != null)
                consumer.accept(this.cache);
        }
        return this;
    }

    void update(T cache) {
        this.cache = cache;
        this.listeners.forEach(c -> c.accept(this.cache));
    }

    public T get() {
        return this.cache;
    }
}
