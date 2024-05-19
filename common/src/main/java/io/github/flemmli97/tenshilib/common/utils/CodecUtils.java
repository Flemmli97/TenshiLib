package io.github.flemmli97.tenshilib.common.utils;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CodecUtils {

    /**
     * Enum codec that uses the enum name as backend
     */
    public static <T extends Enum<T>> Codec<T> stringEnumCodec(Class<T> clss, T fallback) {
        return Codec.STRING.flatXmap(s -> {
            try {
                return DataResult.success(Enum.valueOf(clss, s));
            } catch (IllegalArgumentException e) {
                if (fallback != null)
                    return DataResult.success(fallback);
                return DataResult.error("No such enum constant " + s + " for class " + clss);
            }
        }, e -> DataResult.success(e.name()));
    }

    /**
     * Enum codec that uses the enums ordinal as backend. Prefer this since it's smaller
     */
    public static <T extends Enum<T>> Codec<T> ordinalEnumCodec(Class<T> clss, T fallback) {
        return Codec.INT.flatXmap(s -> {
            try {
                return DataResult.success(clss.getEnumConstants()[s]);
            } catch (ArrayIndexOutOfBoundsException e) {
                if (fallback != null)
                    return DataResult.success(fallback);
                return DataResult.error("No such enum ordinal " + s + " for class " + clss);
            }
        }, e -> DataResult.success(e.ordinal()));
    }

    /**
     * A codec that uses the given gson to as a base.
     *
     * @param gson The Gson instance that handles the value. The instance needs to be able to handle it (e.g. by having a serializer registered)
     */
    public static <E> Codec<E> jsonCodecBuilder(Gson gson, Class<E> clss, String name) {
        return new Codec<>() {
            @Override
            public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
                try {
                    JsonElement e = gson.toJsonTree(input);
                    return DataResult.success(NullableJsonOps.INSTANCE.convertTo(ops, e));
                } catch (JsonParseException err) {
                    return DataResult.error("Couldn't encode value " + input + " error: " + err);
                }
            }

            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
                JsonElement element = ops.convertTo(JsonOps.INSTANCE, input);
                try {
                    E result = gson.fromJson(element, clss);
                    return DataResult.success(Pair.of(result, input));
                } catch (JsonParseException err) {
                    return DataResult.error("Couldn't decode value " + err);
                }
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    /**
     * Get the codec of a custom registry. Custom registries might not exist at initialization time (e.g. when using a static instance)
     * Thus we use a supplier as backend.
     * For vanilla registries this is not needed
     */
    public static <T> Codec<T> registryCodec(ResourceKey<? extends Registry<T>> key) {
        Supplier<Codec<T>> reg = Suppliers.memoize(() -> PlatformUtils.INSTANCE.registry(key).byNameCodec());
        return new Codec<>() {

            @Override
            public <O> DataResult<O> encode(T input, DynamicOps<O> ops, O prefix) {
                return reg.get().encode(input, ops, prefix);
            }

            @Override
            public <I> DataResult<Pair<T, I>> decode(DynamicOps<I> ops, I input) {
                return reg.get().decode(ops, input);
            }

            @Override
            public String toString() {
                return reg.get().toString();
            }
        };
    }

    /**
     * Get the codec of a custom registry mapped to another codec. Custom registries might not exist at initialization time (e.g. when using a static instance)
     * Thus we use a supplier as backend.
     * For vanilla registries this is not needed
     */
    public static <V, T> Codec<V> registryCodec(ResourceKey<? extends Registry<T>> key, Function<Codec<T>, Codec<V>> cons) {
        Supplier<Codec<V>> reg = Suppliers.memoize(() -> cons.apply(PlatformUtils.INSTANCE.registry(key)
                .byNameCodec()));
        return new Codec<>() {

            @Override
            public <O> DataResult<O> encode(V input, DynamicOps<O> ops, O prefix) {
                return reg.get().encode(input, ops, prefix);
            }

            @Override
            public <I> DataResult<Pair<V, I>> decode(DynamicOps<I> ops, I input) {
                return reg.get().decode(ops, input);
            }

            @Override
            public String toString() {
                return reg.get().toString();
            }
        };
    }

    public static class NullableJsonOps extends JsonOps {

        public static final JsonOps INSTANCE = new NullableJsonOps(false);

        protected NullableJsonOps(boolean compressed) {
            super(compressed);
        }

        @Override
        public <U> U convertMap(final DynamicOps<U> ops, JsonElement e) {
            DataResult<Stream<Pair<JsonPrimitive, JsonElement>>> mapLike = DataResult.success(e.getAsJsonObject().entrySet().stream()
                    .filter(entry -> !(entry.getValue() instanceof JsonNull))
                    .map(entry -> Pair.of(new JsonPrimitive(entry.getKey()), entry.getValue())));
            return ops.createMap(mapLike.result().orElse(Stream.empty()).map(entry ->
                    Pair.of(this.convertTo(ops, entry.getFirst()),
                            this.convertTo(ops, entry.getSecond()))
            ));
        }
    }
}
