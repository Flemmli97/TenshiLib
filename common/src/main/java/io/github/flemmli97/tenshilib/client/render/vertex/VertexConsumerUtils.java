package io.github.flemmli97.tenshilib.client.render.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector4f;

public class VertexConsumerUtils {

    public static class ConstantFloatVertexConsumer extends WrappedVertexConsumer {

        private final VertexUtils.VertexFormatElementData<Float> data;
        private final float value;

        public ConstantFloatVertexConsumer(VertexConsumer wrapped, VertexUtils.VertexFormatElementData<Float> data, float value) {
            super(wrapped);
            this.data = data;
            this.value = value;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            super.addVertex(x, y, z);
            return VertexUtils.addVertexData(
                    this.wrapped,
                    this.data.element().get(),
                    this.value
            );
        }
    }

    public static class ConstantVec4fVertexConsumer extends WrappedVertexConsumer {

        private final VertexUtils.VertexFormatElementData<Vector4f> data;
        private final Vector4f value;

        public ConstantVec4fVertexConsumer(VertexConsumer wrapped, VertexUtils.VertexFormatElementData<Vector4f> data, Vector4f value) {
            super(wrapped);
            this.data = data;
            this.value = value;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            super.addVertex(x, y, z);
            return VertexUtils.addVertexData(
                    this.wrapped,
                    this.data.element().get(),
                    this.value.x(), this.value.y(), this.value.z(), this.value.w()
            );
        }
    }
}
