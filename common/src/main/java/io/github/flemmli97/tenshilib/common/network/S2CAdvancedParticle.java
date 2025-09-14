package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleRegistry;
import io.github.flemmli97.tenshilib.common.particle.AdvancedParticleContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record S2CAdvancedParticle(AdvancedParticleContainer container, double x, double y,
                                  double z) implements CustomPacketPayload {

    public static final Type<S2CAdvancedParticle> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "s2c_advanced_particle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CAdvancedParticle> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CAdvancedParticle decode(RegistryFriendlyByteBuf buf) {
            return new S2CAdvancedParticle(AdvancedParticleContainer.STREAM_CODEC.decode(buf),
                    buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CAdvancedParticle pkt) {
            AdvancedParticleContainer.STREAM_CODEC.encode(buf, pkt.container());
            buf.writeDouble(pkt.x());
            buf.writeDouble(pkt.y());
            buf.writeDouble(pkt.z());
        }
    };

    public static void handle(S2CAdvancedParticle pkt, Player player) {
        AdvancedParticleRegistry.createParticle(pkt.container(), pkt.x(), pkt.y(), pkt.z());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}