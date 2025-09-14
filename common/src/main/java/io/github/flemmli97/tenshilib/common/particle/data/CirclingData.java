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
import net.minecraft.world.phys.Vec3;

public record CirclingData(float radius, float radiusIncrease, float angle, float angleIncrease,
                           Vec3 rotationAxis) implements AdvancedParticleData {

    public static final MapCodec<CirclingData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Codec.FLOAT.fieldOf("radius").forGetter(CirclingData::radius),
                    Codec.FLOAT.fieldOf("radius_increase").forGetter(CirclingData::radiusIncrease),
                    Codec.FLOAT.fieldOf("angle").forGetter(CirclingData::angle),
                    Codec.FLOAT.fieldOf("angle_increase").forGetter(CirclingData::angleIncrease),
                    Vec3.CODEC.fieldOf("rotation_axis").forGetter(CirclingData::rotationAxis)
            ).apply(inst, CirclingData::new));
    public static final StreamCodec<ByteBuf, CirclingData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, CirclingData::radius,
            ByteBufCodecs.FLOAT, CirclingData::radiusIncrease, ByteBufCodecs.FLOAT, CirclingData::angle,
            ByteBufCodecs.FLOAT, CirclingData::angleIncrease, AdvancedParticleData.VEC3, CirclingData::rotationAxis, CirclingData::new);

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.CIRLING.get();
    }
}
