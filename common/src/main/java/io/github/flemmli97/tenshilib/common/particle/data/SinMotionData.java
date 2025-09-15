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

public record SinMotionData(Vec3 delta, float offset, float period, boolean add) implements AdvancedParticleData {

    public static final MapCodec<SinMotionData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Vec3.CODEC.fieldOf("delta").forGetter(SinMotionData::delta),
                    Codec.FLOAT.fieldOf("offset").forGetter(SinMotionData::offset),
                    Codec.FLOAT.fieldOf("period").forGetter(SinMotionData::period),
                    Codec.BOOL.fieldOf("add").forGetter(SinMotionData::add)
            ).apply(inst, SinMotionData::new));
    public static final StreamCodec<ByteBuf, SinMotionData> STREAM_CODEC = StreamCodec.composite(StreamCodecs.VEC3, SinMotionData::delta,
            ByteBufCodecs.FLOAT, SinMotionData::offset, ByteBufCodecs.FLOAT, SinMotionData::period,
            ByteBufCodecs.BOOL, SinMotionData::add, SinMotionData::new);

    public SinMotionData(Vec3 delta, float period) {
        this(delta, 0, period, true);
    }

    public SinMotionData(double x, double y, double z, float period) {
        this(new Vec3(x, y, z), 0, period, true);
    }

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.SIN_MOTION.get();
    }
}
