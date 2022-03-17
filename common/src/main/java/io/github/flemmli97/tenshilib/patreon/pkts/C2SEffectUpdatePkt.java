package io.github.flemmli97.tenshilib.patreon.pkts;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.network.Packet;
import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonEffects;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class C2SEffectUpdatePkt implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "c2s_patreon_effect_pkt");

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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        buf.writeBoolean(this.render);
        buf.writeEnum(this.location);
        buf.writeInt(this.color);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public static C2SEffectUpdatePkt fromBytes(FriendlyByteBuf buf) {
        return new C2SEffectUpdatePkt(buf.readUtf(), buf.readBoolean(), buf.readEnum(RenderLocation.class), buf.readInt());
    }

    public static void handlePacketServer(C2SEffectUpdatePkt pkt, ServerPlayer player) {
        PatreonPlatform.INSTANCE.playerSettings(player).ifPresent(settings -> {
            int tier = PatreonDataManager.get(player.getUUID().toString()).tier();
            PatreonEffects.PatreonEffectConfig eff;
            if (tier < 1 || (eff = PatreonEffects.get(pkt.id)) == null || eff.tier > tier) {
                settings.setEffect(null);
            } else {
                settings.read(pkt, pkt.id);
            }
            PatreonPlatform.INSTANCE.sendToTracking(player,
                    new S2CEffectUpdatePkt(player.getId(), settings.effect() != null ? settings.effect().id() : "", settings.shouldRender(), settings.getRenderLocation(), settings.getColor()));
        });
    }
}
