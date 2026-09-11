package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.mixin.BufferSourceAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;
import java.util.function.Function;

/**
 * Use this if modifying an existing {@link MultiBufferSource} with new RenderType mappings.
 * This is because vanillas fixedBuffers assume no draws between RenderType changes.
 * Not using this will cause invalid draw states for fixed buffers!
 */
public class WrappedBufferSource implements MultiBufferSource {

    private final MultiBufferSource delegate;
    private final Function<RenderType, RenderType> renderTypeFunc;
    private final Function<VertexConsumer, VertexConsumer> consumerFunc;

    private final Map<RenderType, ByteBufferBuilder> fixedBuffers;

    public WrappedBufferSource(MultiBufferSource delegate, Function<RenderType, RenderType> renderTypeFunc) {
        this(delegate, renderTypeFunc, null);
    }

    public WrappedBufferSource(MultiBufferSource delegate,
                               Function<RenderType, RenderType> renderTypeFunc, Function<VertexConsumer, VertexConsumer> consumerFunc) {
        this.delegate = delegate;
        this.renderTypeFunc = renderTypeFunc;
        this.consumerFunc = consumerFunc;
        if (this.delegate instanceof BufferSourceAccessor acc) {
            this.fixedBuffers = acc.tenshilib$fixedBuffers();
        } else {
            this.fixedBuffers = null;
        }
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        RenderType newType = this.renderTypeFunc.apply(renderType);
        if (this.fixedBuffers != null && renderType != newType
                && this.fixedBuffers.containsKey(renderType)
                && !this.fixedBuffers.containsKey(newType)) {
            this.fixedBuffers.put(newType, new ByteBufferBuilder(newType.bufferSize()));
        }
        renderType = newType;
        VertexConsumer consumer = this.delegate.getBuffer(renderType);
        if (this.consumerFunc != null) {
            return this.consumerFunc.apply(consumer);
        }
        return consumer;
    }
}
