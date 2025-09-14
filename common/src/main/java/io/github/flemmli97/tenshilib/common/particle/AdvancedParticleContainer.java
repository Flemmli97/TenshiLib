package io.github.flemmli97.tenshilib.common.particle;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleRegistry;
import io.github.flemmli97.tenshilib.common.network.S2CAdvancedParticle;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AdvancedParticleContainer(ParticleOptions options, List<AdvancedParticleData> data) {

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedParticleContainer> STREAM_CODEC = new StreamCodec<>() {
        private final StreamCodec<RegistryFriendlyByteBuf, List<AdvancedParticleData>> collection = ByteBufCodecs.collection(ArrayList::new, AdvancedParticleData.STREAM_CODEC);

        @Override
        public AdvancedParticleContainer decode(RegistryFriendlyByteBuf buf) {
            return new AdvancedParticleContainer(ParticleTypes.STREAM_CODEC.decode(buf), this.collection.decode(buf)
                    .stream().filter(Objects::nonNull).toList());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, AdvancedParticleContainer data) {
            ParticleTypes.STREAM_CODEC.encode(buf, data.options);
            this.collection.encode(buf, data.data);
        }
    };

    public static Builder make(ParticleOptions options) {
        return new Builder(options);
    }

    public void add(Level level, double x, double y, double z) {
        this.add(level, null, x, y, z, false);
    }

    public void add(Level level, @Nullable Player player, double x, double y, double z, boolean longDistance) {
        if (!(level instanceof ServerLevel serverLevel)) {
            AdvancedParticleRegistry.createParticle(this, x, y, z);
        } else {
            List<ServerPlayer> players = player != null ? List.of((ServerPlayer) player) : serverLevel.players();
            CustomPacketPayload pkt = new S2CAdvancedParticle(this, x, y, z);
            for (ServerPlayer p : players) {
                BlockPos pos = p.blockPosition();
                if (pos.closerToCenterThan(new Vec3(x, y, z), longDistance ? 512 : 32)) {
                    LoaderNetwork.INSTANCE.sendToPlayer(pkt, p);
                }
            }
        }
    }

    public static class Builder {

        private final ParticleOptions options;
        private final List<AdvancedParticleData> data = new ArrayList<>();

        private Builder(ParticleOptions options) {
            this.options = options;
        }

        public Builder addData(AdvancedParticleData data) {
            this.data.add(data);
            return this;
        }

        public AdvancedParticleContainer build() {
            return new AdvancedParticleContainer(this.options, List.copyOf(this.data));
        }
    }
}
