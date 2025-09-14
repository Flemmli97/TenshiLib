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

public record ScaleData(float start, float end, int duration) implements AdvancedParticleData {

    public static final MapCodec<ScaleData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Codec.FLOAT.fieldOf("start").forGetter(ScaleData::start),
                    Codec.FLOAT.fieldOf("end").forGetter(ScaleData::end),
                    Codec.INT.fieldOf("duration").forGetter(ScaleData::duration)
            ).apply(inst, ScaleData::new));
    public static final StreamCodec<ByteBuf, ScaleData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, ScaleData::start,
            ByteBufCodecs.FLOAT, ScaleData::end, ByteBufCodecs.INT, ScaleData::duration, ScaleData::new);

    public ScaleData(float scale) {
        this(scale, scale, 0);
    }

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.SCALE.get();
    }
}
