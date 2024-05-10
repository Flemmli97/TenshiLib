package io.github.flemmli97.tenshilib.patreon.pkts;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class S2CEffectUpdatePkt extends C2SEffectUpdatePkt {

    public static final Type<S2CEffectUpdatePkt> TYPE = new Type<>(new ResourceLocation(TenshiLib.MODID, "s2c_patreon_effect_pkt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CEffectUpdatePkt> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CEffectUpdatePkt decode(RegistryFriendlyByteBuf buf) {
            return new S2CEffectUpdatePkt(buf.readInt(), buf.readUtf(), buf.readBoolean(), buf.readEnum(RenderLocation.class), buf.readInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CEffectUpdatePkt pkt) {
            buf.writeInt(pkt.entityID);
            buf.writeUtf(pkt.id);
            buf.writeBoolean(pkt.render);
            buf.writeEnum(pkt.location);
            buf.writeInt(pkt.color);
        }
    };
    public final int entityID;

    public S2CEffectUpdatePkt(int entityID, String id, boolean render, RenderLocation location, int color) {
        super(id, render, location, color);
        this.entityID = entityID;
    }

    public static class Handler {
        public static void handlePacket(S2CEffectUpdatePkt pkt, Player player) {
            if (pkt.entityID != player.getId()) {
                Entity e = player.level().getEntity(pkt.entityID);
                if (e instanceof Player)
                    player = (Player) e;
            }
            PatreonPlatform.INSTANCE.playerSettings(player).read(pkt, pkt.id);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
