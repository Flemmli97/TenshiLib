package io.github.flemmli97.tenshilib.common.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ColoredParticleType extends ParticleType<ColoredParticleData> {

    private final MapCodec<ColoredParticleData> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, ColoredParticleData> streamCodec;

    public ColoredParticleType(boolean alwaysShow) {
        super(alwaysShow);
        this.codec = ColoredParticleData.codec(this);
        this.streamCodec = ColoredParticleData.streamCodec(this);
    }

    @Override
    public MapCodec<ColoredParticleData> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ColoredParticleData> streamCodec() {
        return this.streamCodec;
    }
}
