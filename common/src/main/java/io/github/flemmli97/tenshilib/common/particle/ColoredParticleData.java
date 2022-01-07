package io.github.flemmli97.tenshilib.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.tenshilib.RegistryHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class ColoredParticleData implements ParticleOptions {

    public static Codec<ColoredParticleData> codec(ParticleType<ColoredParticleData> type) {
        return RecordCodecBuilder.create((builder) -> builder.group(
                Codec.FLOAT.fieldOf("r").forGetter(ColoredParticleData::getRed),
                Codec.FLOAT.fieldOf("g").forGetter(ColoredParticleData::getGreen),
                Codec.FLOAT.fieldOf("b").forGetter(ColoredParticleData::getBlue),
                Codec.FLOAT.fieldOf("alpha").forGetter(ColoredParticleData::getAlpha),
                Codec.FLOAT.fieldOf("scale").forGetter(ColoredParticleData::getScale))
                .apply(builder, (r, g, b, a, scale) -> new ColoredParticleData(type, r, g, b, a, scale)));
    }

    public static final Deserializer<ColoredParticleData> DESERIALIZER = new Deserializer<>() {
        @Override
        public ColoredParticleData fromCommand(ParticleType<ColoredParticleData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            reader.expect(' ');
            float a = reader.readFloat();
            reader.expect(' ');
            float scale = reader.readFloat();
            return new ColoredParticleData(type, r, g, b, a, scale);
        }

        @Override
        public ColoredParticleData fromNetwork(ParticleType<ColoredParticleData> type, FriendlyByteBuf buffer) {
            return new ColoredParticleData(type, buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        }
    };


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

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.red);
        buffer.writeFloat(this.green);
        buffer.writeFloat(this.blue);
        buffer.writeFloat(this.alpha);
        buffer.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return RegistryHelper.particles().getIDFrom(this.getType()).toString();
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