package io.github.flemmli97.tenshilib.common.particle;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class ColoredParticleType extends ParticleType<ColoredParticleData> {

    private final Codec<ColoredParticleData> codec;

    public ColoredParticleType(boolean alwaysShow) {
        super(alwaysShow, ColoredParticleData.DESERIALIZER);
        this.codec = ColoredParticleData.codec(this);
    }

    @Override
    public Codec<ColoredParticleData> codec() {
        return this.codec;
    }
}
