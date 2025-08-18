package io.github.flemmli97.tenshilib.common.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple ResourceLocation with generic contraint.
 * Useful if some more type checks are needed
 */
public record TypedResource<T>(ResourceLocation location) {

    public static final StreamCodec<ByteBuf, TypedResource<?>> STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .map(TypedResource::new, TypedResource::location);
}
