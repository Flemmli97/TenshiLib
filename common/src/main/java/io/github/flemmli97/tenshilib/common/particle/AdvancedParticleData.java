package io.github.flemmli97.tenshilib.common.particle;

import com.mojang.serialization.Codec;
import io.github.flemmli97.tenshilib.common.registry.TenshilibParticleHandlerTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public interface AdvancedParticleData {

    Codec<AdvancedParticleData> CODEC = TenshilibParticleHandlerTypes.PARTICLE_HANDLER_TYPES.registry().byNameCodec()
            .dispatch(AdvancedParticleData::getType, ParticleHandlerType::codec);
    StreamCodec<RegistryFriendlyByteBuf, AdvancedParticleData> STREAM_CODEC = ByteBufCodecs.registry(TenshilibParticleHandlerTypes.PARTICLE_HANDLER_TYPES_KEY)
            .dispatch(AdvancedParticleData::getType, ParticleHandlerType::streamCodec);

    StreamCodec<ByteBuf, Vector4f> VECTOR_4F_STREAM = new StreamCodec<>() {
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
    StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<>() {
        @Override
        public Vec3 decode(ByteBuf buf) {
            return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void encode(ByteBuf buf, Vec3 data) {
            buf.writeDouble(data.x());
            buf.writeDouble(data.y());
            buf.writeDouble(data.x());
        }
    };

    ParticleHandlerType<?> getType();
}
