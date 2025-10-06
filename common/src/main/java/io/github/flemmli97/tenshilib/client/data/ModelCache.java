package io.github.flemmli97.tenshilib.client.data;

import io.github.flemmli97.tenshilib.client.model.BedrockGeometryParser;
import io.github.flemmli97.tenshilib.client.model.DeformationChange;
import io.github.flemmli97.tenshilib.client.model.ModelPartsContainer;

import java.util.IdentityHashMap;
import java.util.function.Consumer;

public class ModelCache {

    private final IdentityHashMap<DeformationChange, ReloadableCache<ModelPartsContainer>> deformCache = new IdentityHashMap<>();

    private BedrockGeometryParser.BedrockGeometry geometry;

    void update(BedrockGeometryParser.BedrockGeometry cache) {
        this.geometry = cache;
        this.deformCache.forEach((deform, reload) -> reload.update(this.get().bake(deform)));
    }

    ReloadableCache<ModelPartsContainer> bake(DeformationChange deformation, Consumer<ModelPartsContainer> onChange) {
        return this.deformCache.computeIfAbsent(deformation, d -> {
            ReloadableCache<ModelPartsContainer> cache = new ReloadableCache<>();
            if (this.geometry != null) {
                cache.update(this.geometry.bake(deformation));
            }
            return cache;
        }).onChange(onChange);
    }

    public BedrockGeometryParser.BedrockGeometry get() {
        return this.geometry;
    }
}
