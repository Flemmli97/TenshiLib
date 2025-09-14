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

public record ParticleMetaData(int duration, boolean physics, float gravityScale) implements AdvancedParticleData {

    public static final MapCodec<ParticleMetaData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Codec.INT.fieldOf("duration").forGetter(ParticleMetaData::duration),
                    Codec.BOOL.fieldOf("physics").forGetter(ParticleMetaData::physics),
                    Codec.FLOAT.fieldOf("gravity").forGetter(ParticleMetaData::gravityScale)
            ).apply(inst, ParticleMetaData::new));
    public static final StreamCodec<ByteBuf, ParticleMetaData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, ParticleMetaData::duration,
            ByteBufCodecs.BOOL, ParticleMetaData::physics, ByteBufCodecs.FLOAT, ParticleMetaData::gravityScale, ParticleMetaData::new);


    public ParticleMetaData(int duration) {
        this(duration, true, 1);
    }

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.PARTICLE_META.get();
    }
}
