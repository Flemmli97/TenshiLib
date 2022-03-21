package io.github.flemmli97.tenshilib.patreon.pkts;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class S2CEffectUpdatePkt extends C2SEffectUpdatePkt {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "s2c_patreon_effect_pkt");

    public final int entityID;

    public S2CEffectUpdatePkt(int entityID, String id, boolean render, RenderLocation location, int color) {
        super(id, render, location, color);
        this.entityID = entityID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeUtf(this.id);
        buf.writeBoolean(this.render);
        buf.writeEnum(this.location);
        buf.writeInt(this.color);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public static S2CEffectUpdatePkt fromBytes(FriendlyByteBuf buf) {
        return new S2CEffectUpdatePkt(buf.readInt(), buf.readUtf(), buf.readBoolean(), buf.readEnum(RenderLocation.class), buf.readInt());
    }

    public static class Handler {
        public static void handlePktClient(S2CEffectUpdatePkt pkt) {
            Player player = ClientHandlers.clientPlayer();
            if (pkt.entityID != player.getId()) {
                Entity e = player.level.getEntity(pkt.entityID);
                if (e instanceof Player)
                    player = (Player) e;
            }
            PatreonPlatform.INSTANCE.playerSettings(player).ifPresent(settings -> settings.read(pkt, pkt.id));
        }
    }
}
