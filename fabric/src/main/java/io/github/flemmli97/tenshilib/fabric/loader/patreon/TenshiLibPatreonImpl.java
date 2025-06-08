package io.github.flemmli97.tenshilib.fabric.loader.patreon;

import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.TenshiLibPatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.pkts.S2CEffectUpdatePkt;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TenshiLibPatreonImpl implements TenshiLibPatreonPlatform {

    public static void initPatreonData() {
        ServerEntityEvents.ENTITY_LOAD.register(TenshiLibPatreonImpl::onLogin);
        PatreonDataManager.init();
    }

    public static void onLogin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            TenshiLibPatreonPlatform.INSTANCE.playerSettings(player).setToDefault(false);
            TenshiLibPatreonPlatform.INSTANCE.sendToClient(player, player);
        }
    }

    @Override
    public PatreonPlayerSetting playerSettings(Player player) {
        return ((PlayerPatreonData) player).tenshilib$Settings();
    }

    @Override
    public void sendToClient(ServerPlayer player, ServerPlayer target) {
        PatreonPlayerSetting setting = TenshiLibPatreonPlatform.INSTANCE.playerSettings(target);
        if (PatreonDataManager.get(target.getUUID().toString()).tier() < 1)
            setting.setEffect(null);
        if (ServerPlayNetworking.canSend(player, S2CEffectUpdatePkt.TYPE)) {
            S2CEffectUpdatePkt pkt = new S2CEffectUpdatePkt(target.getId(), setting.effect() != null ? setting.effect().id() : "", setting.shouldRender(), setting.getRenderLocation(), setting.getColor());
            ServerPlayNetworking.send(player, pkt);
        }
    }
}
