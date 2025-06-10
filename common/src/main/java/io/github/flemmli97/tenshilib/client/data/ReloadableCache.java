package io.github.flemmli97.tenshilib.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReloadableCache<T> {

    private final List<Consumer<T>> listeners = new ArrayList<>();

    private T cache;

    ReloadableCache<T> onChange(Consumer<T> consumer) {
        if (consumer != null)
            this.listeners.add(consumer);
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
