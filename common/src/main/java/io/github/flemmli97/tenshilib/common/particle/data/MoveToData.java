package io.github.flemmli97.tenshilib.common.particle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.tenshilib.common.particle.AdvancedParticleData;
import io.github.flemmli97.tenshilib.common.particle.ParticleHandlerType;
import io.github.flemmli97.tenshilib.common.registry.TenshilibParticleHandlerTypes;
import io.github.flemmli97.tenshilib.common.utils.StreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record MoveToData(Vec3 target, int duration) implements AdvancedParticleData {

    public static final MapCodec<MoveToData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Vec3.CODEC.fieldOf("target").forGetter(MoveToData::target),
                    Codec.INT.fieldOf("duration").forGetter(MoveToData::duration)
            ).apply(inst, MoveToData::new));
    public static final StreamCodec<ByteBuf, MoveToData> STREAM_CODEC = StreamCodec.composite(StreamCodecs.VEC3, MoveToData::target,
            ByteBufCodecs.INT, MoveToData::duration, MoveToData::new);

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.MOVE_TO.get();
    }
}
