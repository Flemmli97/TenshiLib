package io.github.flemmli97.tenshilib.client.data;

public class ReloadableCache<T> {

    private T cache;

    void update(T cache) {
        this.cache = cache;
    }

    public T get() {
        return this.cache;
    }
}
