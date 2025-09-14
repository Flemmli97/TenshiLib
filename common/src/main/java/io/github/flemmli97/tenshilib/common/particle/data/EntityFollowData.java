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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record EntityFollowData(int entity, Optional<Vec3> offset) implements AdvancedParticleData {

    public static final MapCodec<EntityFollowData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Codec.INT.fieldOf("entity").forGetter(EntityFollowData::entity),
                    Vec3.CODEC.optionalFieldOf("offset").forGetter(EntityFollowData::offset)
            ).apply(inst, EntityFollowData::new));
    public static final StreamCodec<ByteBuf, EntityFollowData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EntityFollowData::entity,
            ByteBufCodecs.optional(StreamCodecs.VEC3), EntityFollowData::offset, EntityFollowData::new);

    public EntityFollowData(Entity entity) {
        this(entity.getId(), Optional.empty());
    }

    public EntityFollowData(Entity entity, Vec3 offset) {
        this(entity.getId(), Optional.of(offset));
    }

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.ENTITY_FOLLOW.get();
    }
}
