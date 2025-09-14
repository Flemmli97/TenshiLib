package io.github.flemmli97.tenshilib.common.utils;

import io.github.flemmli97.tenshilib.common.entity.ai.TargetPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public class StreamCodecs {

    public static final StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<>() {
        @Override
        public Vec3 decode(ByteBuf buffer) {
            return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        @Override
        public void encode(ByteBuf buffer, Vec3 value) {
            buffer.writeDouble(value.x());
            buffer.writeDouble(value.y());
            buffer.writeDouble(value.z());
        }
    };

    public static final StreamCodec<ByteBuf, Vector4f> VECTOR_4F_STREAM = new StreamCodec<>() {
        @Override
        public Vector4f decode(ByteBuf buf) {
            return new Vector4f(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void encode(ByteBuf buf, Vector4f data) {
            buf.writeFloat(data.x());
            buf.writeFloat(data.y());
            buf.writeFloat(data.x());
            buf.writeFloat(data.w());
        }
    };

    public static final StreamCodec<ByteBuf, TargetPosition> TARGET_POSITION = new StreamCodec<>() {
        @Override
        public TargetPosition decode(ByteBuf buffer) {
            return new TargetPosition(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                    buffer.readDouble(), buffer.readDouble());
        }

        @Override
        public void encode(ByteBuf buffer, TargetPosition value) {
            buffer.writeDouble(value.position().x());
            buffer.writeDouble(value.position().y());
            buffer.writeDouble(value.position().z());
            buffer.writeDouble(value.minHeight());
            buffer.writeDouble(value.maxHeight());
        }
    };
}
