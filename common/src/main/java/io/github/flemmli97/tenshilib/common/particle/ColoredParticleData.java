package io.github.flemmli97.tenshilib.common.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ColoredParticleData implements ParticleOptions {

    public static MapCodec<ColoredParticleData> codec(ParticleType<ColoredParticleData> type) {
        return RecordCodecBuilder.mapCodec((builder) -> builder.group(
                        Codec.FLOAT.fieldOf("r").forGetter(ColoredParticleData::getRed),
                        Codec.FLOAT.fieldOf("g").forGetter(ColoredParticleData::getGreen),
                        Codec.FLOAT.fieldOf("b").forGetter(ColoredParticleData::getBlue),
                        Codec.FLOAT.fieldOf("alpha").forGetter(ColoredParticleData::getAlpha),
                        Codec.FLOAT.fieldOf("scale").forGetter(ColoredParticleData::getScale))
                .apply(builder, (r, g, b, a, scale) -> new ColoredParticleData(type, r, g, b, a, scale)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ColoredParticleData> streamCodec(ParticleType<ColoredParticleData> type) {
        return new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ColoredParticleData data) {
                buffer.writeFloat(data.red);
                buffer.writeFloat(data.green);
                buffer.writeFloat(data.blue);
                buffer.writeFloat(data.alpha);
                buffer.writeFloat(data.scale);
            }

            @Override
            public ColoredParticleData decode(RegistryFriendlyByteBuf buffer) {
                return new ColoredParticleData(type, buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
            }
        };
    }


    private final ParticleType<? extends ColoredParticleData> type;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final float scale;

    public ColoredParticleData(ParticleType<? extends ColoredParticleData> type, float red, float green, float blue, float alpha) {
        this(type, red, green, blue, alpha, 1);
    }

    public ColoredParticleData(ParticleType<? extends ColoredParticleData> type, float red, float green, float blue, float alpha, float scale) {
        this.type = type;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.scale = scale;
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public float getScale() {
        return this.scale;
    }
}