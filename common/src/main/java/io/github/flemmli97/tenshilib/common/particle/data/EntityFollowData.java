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

/**
 * Makes the particle follow the given entity
 *
 * @param offset         The offset to the entity. Ignored if differenceOnly is true
 * @param differenceOnly If true the particle only moves the amount the entity moved between the last tick
 */
public record EntityFollowData(int entity, Optional<Vec3> offset,
                               boolean differenceOnly) implements AdvancedParticleData {

    public static final MapCodec<EntityFollowData> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(Codec.INT.fieldOf("entity").forGetter(EntityFollowData::entity),
                    Vec3.CODEC.optionalFieldOf("offset").forGetter(EntityFollowData::offset),
                    Codec.BOOL.fieldOf("difference_only").forGetter(EntityFollowData::differenceOnly)
            ).apply(inst, EntityFollowData::new));
    public static final StreamCodec<ByteBuf, EntityFollowData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EntityFollowData::entity, ByteBufCodecs.optional(StreamCodecs.VEC3), EntityFollowData::offset,
            ByteBufCodecs.BOOL, EntityFollowData::differenceOnly, EntityFollowData::new);

    public EntityFollowData(Entity entity) {
        this(entity.getId(), Optional.empty(), false);
    }

    public EntityFollowData(Entity entity, Vec3 offset) {
        this(entity.getId(), Optional.of(offset), false);
    }

    public EntityFollowData(Entity entity, boolean differenceOnly) {
        this(entity.getId(), Optional.empty(), differenceOnly);
    }

    @Override
    public ParticleHandlerType<?> getType() {
        return TenshilibParticleHandlerTypes.ENTITY_FOLLOW.get();
    }
}
