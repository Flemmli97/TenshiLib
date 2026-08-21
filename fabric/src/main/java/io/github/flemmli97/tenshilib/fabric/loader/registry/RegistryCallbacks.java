package io.github.flemmli97.tenshilib.fabric.loader.registry;

import com.google.common.collect.ImmutableMap;
import io.github.flemmli97.tenshilib.fabric.mixin.PoiTypesAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.util.Map;

/**
 * Do things for some registries.
 * Use fabric-apis stuff
 */
public class RegistryCallbacks {

    private static final Map<ResourceKey<? extends Registry<?>>, Callback<?>> CALLBACKS = callbacks();

    private static Map<ResourceKey<? extends Registry<?>>, Callback<?>> callbacks() {
        ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, Callback<?>> builder = ImmutableMap.builder();
        registerCallback(builder, Registries.POINT_OF_INTEREST_TYPE, holder -> PoiTypesAccessor.tenshilib$registerBlockStates(holder, holder.value().matchingStates()));
        return builder.build();
    }

    private static <T> void registerCallback(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, Callback<?>> builder,
                                             ResourceKey<Registry<T>> key, Callback<T> callback) {
        builder.put(key, callback);
    }

    @SuppressWarnings("unchecked")
    public static <T> void onRegister(ResourceKey<? extends Registry<T>> key, Holder<T> holder) {
        Callback<T> callback = (Callback<T>) CALLBACKS.get(key);
        if (callback != null) {
            callback.onRegister(holder);
        }
    }

    public interface Callback<T> {

        void onRegister(Holder<T> holder);
    }
}
