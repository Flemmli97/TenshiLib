package io.github.flemmli97.tenshilib.common.particle;

import com.mojang.serialization.Codec;
import io.github.flemmli97.tenshilib.common.registry.TenshilibParticleHandlerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface AdvancedParticleData {

    Codec<AdvancedParticleData> CODEC = TenshilibParticleHandlerTypes.PARTICLE_HANDLER_TYPES.registry().byNameCodec()
            .dispatch(AdvancedParticleData::getType, ParticleHandlerType::codec);
    StreamCodec<RegistryFriendlyByteBuf, AdvancedParticleData> STREAM_CODEC = ByteBufCodecs.registry(TenshilibParticleHandlerTypes.PARTICLE_HANDLER_TYPES_KEY)
            .dispatch(AdvancedParticleData::getType, ParticleHandlerType::streamCodec);

    ParticleHandlerType<?> getType();
}
