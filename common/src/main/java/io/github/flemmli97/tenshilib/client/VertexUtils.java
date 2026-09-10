package io.github.flemmli97.tenshilib.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.flemmli97.tenshilib.mixin.VertexFormatElementAccessor;

import java.util.function.Supplier;

/**
 * Use {@link io.github.flemmli97.tenshilib.client.render.vertex.VertexUtils}
 */
@Deprecated
public class VertexUtils {

    public static final Supplier<VertexFormatElement> SINGLE_FLOAT = io.github.flemmli97.tenshilib.client.render.vertex.VertexUtils.SINGLE_FLOAT.element();
    public static final Supplier<VertexFormatElement> VEC4f = io.github.flemmli97.tenshilib.client.render.vertex.VertexUtils.VEC4f.element();

    public static VertexFormatElement register(VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
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
        return io.github.flemmli97.tenshilib.client.render.vertex.VertexUtils.addVertexData(consumer, element, data);
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, int... data) {
        return io.github.flemmli97.tenshilib.client.render.vertex.VertexUtils.addVertexData(consumer, element, data);
    }

    public static VertexConsumer addVertexData(VertexConsumer consumer, VertexFormatElement element, float... data) {
        return io.github.flemmli97.tenshilib.client.render.vertex.VertexUtils.addVertexData(consumer, element, data);
    }
}
