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

public record MotionData(Vec3 delta, boolean constant, boolean add) implements AdvancedParticleData {

    public static final MapCodec<MotionData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Vec3.CODEC.fieldOf("delta").forGetter(MotionData::delta),
                    Codec.BOOL.fieldOf("constant").forGetter(MotionData::constant),
                    Codec.BOOL.fieldOf("add").forGetter(MotionData::add)
            ).apply(inst, MotionData::new));
    public static final StreamCodec<ByteBuf, MotionData> STREAM_CODEC = StreamCodec.composite(StreamCodecs.VEC3, MotionData::delta,
            ByteBufCodecs.BOOL, MotionData::constant, ByteBufCodecs.BOOL, MotionData::add, MotionData::new);

    public MotionData(Vec3 delta) {
        this(delta, false, false);
    }

    public MotionData(double x, double y, double z) {
        this(new Vec3(x, y, z), false, false);
    }

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.MOTION.get();
    }
}
