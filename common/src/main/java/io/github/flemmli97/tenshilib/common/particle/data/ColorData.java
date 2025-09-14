package io.github.flemmli97.tenshilib.common.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.tenshilib.common.particle.AdvancedParticleData;
import io.github.flemmli97.tenshilib.common.particle.ParticleHandlerType;
import io.github.flemmli97.tenshilib.common.registry.TenshilibParticleHandlerTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector4f;

import java.util.Optional;

public record ColorData(Vector4f start, Optional<Vector4f> end, int duration) implements AdvancedParticleData {

    public static final MapCodec<ColorData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(ExtraCodecs.VECTOR4F.fieldOf("start_color").forGetter(ColorData::start),
                    ExtraCodecs.VECTOR4F.optionalFieldOf("end_color").forGetter(ColorData::end),
                    Codec.INT.fieldOf("duration").forGetter(ColorData::duration)
            ).apply(inst, ColorData::new));
    public static final StreamCodec<ByteBuf, ColorData> STREAM_CODEC = StreamCodec.composite(
            AdvancedParticleData.VECTOR_4F_STREAM, ColorData::start,
            ByteBufCodecs.optional(AdvancedParticleData.VECTOR_4F_STREAM), ColorData::end,
            ByteBufCodecs.INT, ColorData::duration, ColorData::new);

    public ColorData(float r, float g, float b) {
        this(r, g, b, 1);
    }

    public ColorData(float r, float g, float b, float a) {
        this(new Vector4f(r, g, b, a), Optional.empty(), 0);
    }

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.COLOR.get();
    }
}
