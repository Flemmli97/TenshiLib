package io.github.flemmli97.tenshilib.client.render.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class WrappedVertexConsumer implements VertexConsumer {

    protected final VertexConsumer wrapped;

    public WrappedVertexConsumer(VertexConsumer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.wrapped.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        this.wrapped.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.wrapped.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.wrapped.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int iv1) {
        this.wrapped.setUv2(u, iv1);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        this.wrapped.setNormal(x, y, z);
        return this;
    }
}
