package io.github.flemmli97.tenshilib.client.render.vertex;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.flemmli97.tenshilib.mixin.BufferBuilderAccessor;
import io.github.flemmli97.tenshilib.mixin.VertexFormatElementAccessor;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

/**
 * Various helper to extend functionality of {@link VertexConsumer} with custom data
 */
public class VertexUtils {

    /**
     * Format using a single float. The actual usage depends on the context this is used in.
     * Defined as supplier to not register it unnecessarily
     */
    public static final VertexFormatElementData<Float> SINGLE_FLOAT = register(VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 1, VertexConsumerUtils.ConstantFloatVertexConsumer::new);
    public static final VertexFormatElementData<Vector4f> VEC4f = register(VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.GENERIC, 4, VertexConsumerUtils.ConstantVec4fVertexConsumer::new);

    /**
     * Registers a new {@link VertexFormatElement}
     * Dynamically looksup the next id and uses that
     *
     * @param consumerFactory Factory for ease of use with a VertexConsumer
     */
    public static <T> VertexFormatElementData<T> register(VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count,
                                                          CustomElementConsumerFactory<T> consumerFactory) {
        return new VertexFormatElementData<>(Suppliers.memoize(() -> register(type, usage, count)), consumerFactory);
    }

    private static VertexFormatElement register(VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
        int next = findNextId();
        return VertexFormatElement.register(next, 0, type, usage, count);
    }

    private static int findNextId() {
        VertexFormatElement[] lookup = VertexFormatElementAccessor.fetchIdLookup();
        for (int i = 0; i < lookup.length; i++) {
            if (lookup[i] == null) {
                return i;
            }
        }
        // Might be fine? Idk
        // Dynamically expands vanillas array upon full.
        // Vanilla seems to work fine
        VertexFormatElement[] newLookup = new VertexFormatElement[lookup.length * 2];
        System.arraycopy(lookup, 0, newLookup, 0, lookup.length);
        VertexFormatElementAccessor.updateIdLookup(newLookup);
        return lookup.length;
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, byte... data) {
        if (!(consumer instanceof BufferBuilder builder))
            return consumer;
        if (data.length != element.count() || element.type() != VertexFormatElement.Type.BYTE) {
            throw new IllegalStateException("Data doesn't match VertexFormatElement");
        }
        long l = ((BufferBuilderAccessor) builder).doBeginElement(element);
        for (int i = 0; i < data.length; i++) {
            MemoryUtil.memPutByte(l + (long) i * element.type().size(), data[i]);
        }
        return consumer;
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, int... data) {
        if (!(consumer instanceof BufferBuilder builder))
            return consumer;
        if (data.length != element.count() || element.type() != VertexFormatElement.Type.INT) {
            throw new IllegalStateException("Data doesn't match VertexFormatElement");
        }
        long l = ((BufferBuilderAccessor) builder).doBeginElement(element);
        for (int i = 0; i < data.length; i++) {
            MemoryUtil.memPutInt(l + (long) i * element.type().size(), data[i]);
        }
        return consumer;
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, float... data) {
        if (!(consumer instanceof BufferBuilder builder))
            return consumer;
        if (data.length != element.count() || element.type() != VertexFormatElement.Type.FLOAT) {
            throw new IllegalStateException("Data doesn't match VertexFormatElement");
        }
        long l = ((BufferBuilderAccessor) builder).doBeginElement(element);
        for (int i = 0; i < data.length; i++) {
            MemoryUtil.memPutFloat(l + (long) i * element.type().size(), data[i]);
        }
        return consumer;
    }

    public record VertexFormatElementData<T>(Supplier<VertexFormatElement> element,
                                             CustomElementConsumerFactory<T> consumerFactory) {

        public VertexConsumer create(VertexConsumer consumer, T data) {
            return this.consumerFactory().apply(consumer, this, data);
        }
    }

    public interface CustomElementConsumerFactory<T> {

        VertexConsumer apply(VertexConsumer consumer, VertexFormatElementData<T> data, T t);
    }
}
