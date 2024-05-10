package io.github.flemmli97.tenshilib.fabric.platform.patreon;

import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.pkts.S2CEffectUpdatePkt;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PatreonImpl implements PatreonPlatform {

    public static void initPatreonData() {
        ServerEntityEvents.ENTITY_LOAD.register(PatreonImpl::onLogin);
        PatreonDataManager.init();
    }

    public static void onLogin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            PatreonPlatform.INSTANCE.playerSettings(player).setToDefault(false);
            PatreonPlatform.INSTANCE.sendToClient(player, player);
        }
    }

    @Override
    public PatreonPlayerSetting playerSettings(Player player) {
        return ((PlayerPatreonData) player).settings();
    }

    @Override
    public void sendToClient(ServerPlayer player, ServerPlayer target) {
        PatreonPlayerSetting setting = PatreonPlatform.INSTANCE.playerSettings(target);
        if (PatreonDataManager.get(target.getUUID().toString()).tier() < 1)
            setting.setEffect(null);
        if (ServerPlayNetworking.canSend(player, S2CEffectUpdatePkt.TYPE)) {
            S2CEffectUpdatePkt pkt = new S2CEffectUpdatePkt(target.getId(), setting.effect() != null ? setting.effect().id() : "", setting.shouldRender(), setting.getRenderLocation(), setting.getColor());
            ServerPlayNetworking.send(player, pkt);
        }
    }

    @Override
    public void sendToTracking(ServerPlayer player, S2CEffectUpdatePkt pkt) {
        PlayerLookup.tracking(player).forEach(sp -> ServerPlayNetworking.send(sp, pkt));
    }
}
