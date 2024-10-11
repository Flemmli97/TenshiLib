package io.github.flemmli97.tenshilib.patreon.pkts;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.effects.PatreonEffectConfig;
import io.github.flemmli97.tenshilib.patreon.effects.PatreonEffects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class C2SEffectUpdatePkt implements CustomPacketPayload {

    public static final Type<C2SEffectUpdatePkt> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "c2s_patreon_effect_pkt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SEffectUpdatePkt> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public C2SEffectUpdatePkt decode(RegistryFriendlyByteBuf buf) {
            return new C2SEffectUpdatePkt(buf.readUtf(), buf.readBoolean(), buf.readEnum(RenderLocation.class), buf.readInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, C2SEffectUpdatePkt pkt) {
            buf.writeUtf(pkt.id);
            buf.writeBoolean(pkt.render);
            buf.writeEnum(pkt.location);
            buf.writeInt(pkt.color);
        }
    };

    public final String id;
    public final boolean render;
    public final RenderLocation location;
    public final int color;

    public C2SEffectUpdatePkt(String id, boolean render, RenderLocation location, int color) {
        this.id = id;
        this.render = render;
        this.location = location;
        this.color = color;
    }

    public static void handlePacket(C2SEffectUpdatePkt pkt, ServerPlayer player) {
        PatreonPlayerSetting setting = PatreonPlatform.INSTANCE.playerSettings(player);
        int tier = PatreonDataManager.get(player.getUUID().toString()).tier();
        PatreonEffectConfig eff;
        if (tier < 1 || (eff = PatreonEffects.get(pkt.id)) == null || eff.tier > tier) {
            setting.setEffect(null);
        } else {
            setting.read(pkt, pkt.id);
        }
        PatreonPlatform.INSTANCE.sendToTracking(player,
                new S2CEffectUpdatePkt(player.getId(), setting.effect() != null ? setting.effect().id() : "", setting.shouldRender(), setting.getRenderLocation(), setting.getColor()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
