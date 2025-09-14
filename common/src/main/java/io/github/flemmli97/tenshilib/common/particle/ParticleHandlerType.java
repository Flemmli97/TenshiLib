package io.github.flemmli97.tenshilib.common.particle;

import com.mojang.serialization.MapCodec;
import io.github.flemmli97.tenshilib.common.registry.TenshilibParticleHandlerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ParticleHandlerType<T extends AdvancedParticleData>(MapCodec<T> codec,
                                                                  StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {

    @Override
    public String toString() {
        return String.format("ParticleHandlerType[%s]", TenshilibParticleHandlerTypes.PARTICLE_HANDLER_TYPES.registry().getKey(this));
    }
}
