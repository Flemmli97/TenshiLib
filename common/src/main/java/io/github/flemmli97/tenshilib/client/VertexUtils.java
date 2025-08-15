package io.github.flemmli97.tenshilib.client;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.flemmli97.tenshilib.mixin.BufferBuilderAccessor;
import io.github.flemmli97.tenshilib.mixin.VertexFormatElementMixin;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

/**
 * Various helper to extend functionality of {@link VertexConsumer} with custom data
 */
public class VertexUtils {

    /**
     * Format using a single float. The actual usage depends on the context this is used in.
     * As supplier to not register it unneccessarily
     */
    public static final Supplier<VertexFormatElement> SINGLE_FLOAT = Suppliers.memoize(()->register(VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1));

    /**
     * Registers a new {@link VertexFormatElement}
     * Dynamically looksup the next id and uses that
     */
    public static VertexFormatElement register(VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
        int next = findNextId();
        return VertexFormatElement.register(next, 0, type, usage, count);
    }

    private static int findNextId() {
        VertexFormatElement[] lookup = VertexFormatElementMixin.fetchIdLookup();
        for (int i = 0; i < lookup.length; i++) {
            if (lookup[i] == null) {
                return i;
            }
        }
        throw new IllegalStateException("VertexFormatElement count limit exceeded");
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, byte... data) {
        if(!(consumer instanceof BufferBuilder builder))
            return consumer;
        if (data.length != element.count() || element.type() != VertexFormatElement.Type.BYTE)
            return consumer;
        long l = ((BufferBuilderAccessor)builder).doBeginElement(element);
        for(int i = 0; i < data.length; i++) {
            MemoryUtil.memPutByte(l + i, data[i]);
        }
        return consumer;
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, int... data) {
        if(!(consumer instanceof BufferBuilder builder))
            return consumer;
        if (data.length != element.count() || element.type() != VertexFormatElement.Type.INT)
            return consumer;
        long l = ((BufferBuilderAccessor)builder).doBeginElement(element);
        for(int i = 0; i < data.length; i++) {
            MemoryUtil.memPutInt(l + i, data[i]);
        }
        return consumer;
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, float... data) {
        if(!(consumer instanceof BufferBuilder builder))
            return consumer;
        if (data.length != element.count() || element.type() != VertexFormatElement.Type.FLOAT)
            return consumer;
        long l = ((BufferBuilderAccessor)builder).doBeginElement(element);
        for(int i = 0; i < data.length; i++) {
            MemoryUtil.memPutFloat(l + i, data[i]);
        }
        return consumer;
    }
}
