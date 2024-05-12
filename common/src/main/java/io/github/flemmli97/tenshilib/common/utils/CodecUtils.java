package io.github.flemmli97.tenshilib.common.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class CodecUtils {

    /**
     * Custom ItemStack Codec that tries to minimize data saved
     */
    public static final Codec<ItemStack> ITEM_STACK_CODEC = tryCodec(ItemStack.ITEM_NON_AIR_CODEC
                    .flatXmap(h -> DataResult.success(new ItemStack(h)),
                            s -> s.getComponentsPatch().isEmpty() && s.getCount() == 1 ? DataResult.success(s.getItemHolder()) : DataResult.error(() -> "Not default itemstack")),
            RecordCodecBuilder.create(inst -> inst.group(
                    ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(stack -> stack.getCount() == 1 ? Optional.empty() : Optional.of(stack.getCount())),
                    DataComponentPatch.CODEC.optionalFieldOf("components").forGetter((stack) -> stack.getComponentsPatch().isEmpty() ? Optional.empty() : Optional.of(stack.getComponentsPatch()))
            ).apply(inst, (s, count, comp) -> new ItemStack(s, count.orElse(1), comp.orElse(DataComponentPatch.EMPTY)))));

    /**
     * Tries the first codec first before attempting the second.
     * Unlike vanilla either codec this also does it for serialization
     */
    public static <F> Codec<F> tryCodec(Codec<F> first, Codec<F> second) {
        return new TryCodec<>(first, second);
    }

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
                return DataResult.error(() -> "No such enum constant " + s + " for class " + clss);
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
                return DataResult.error(() -> "No such enum ordinal " + s + " for class " + clss);
            }
        }, e -> DataResult.success(e.ordinal()));
    }

    private record TryCodec<F>(Codec<F> first, Codec<F> second) implements Codec<F> {

        @Override
        public <T> DataResult<Pair<F, T>> decode(final DynamicOps<T> ops, final T input) {
            final DataResult<Pair<F, T>> first = this.first.decode(ops, input);
            if (first.isSuccess()) {
                return first;
            }
            final DataResult<Pair<F, T>> second = this.second.decode(ops, input);
            if (second.isSuccess()) {
                return second;
            }
            return first.apply2((f, s) -> s, second);
        }

        @Override
        public <T> DataResult<T> encode(F input, final DynamicOps<T> ops, final T prefix) {
            DataResult<T> first = this.first.encode(input, ops, prefix);
            if (first.isSuccess())
                return first;
            DataResult<T> second = this.second.encode(input, ops, prefix);
            if (second.isSuccess())
                return second;
            return first.apply2((f, s) -> s, second);
        }
    }
}
