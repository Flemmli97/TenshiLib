package io.github.flemmli97.tenshilib.patreon;

import io.github.flemmli97.tenshilib.patreon.pkts.S2CEffectUpdatePkt;
import io.github.flemmli97.tenshilib.platform.InitUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public interface PatreonPlatform {

    PatreonPlatform INSTANCE = InitUtil.getPlatformInstance(PatreonPlatform.class,
            "io.github.flemmli97.tenshilib.fabric.platform.patreon.PatreonImpl",
            "io.github.flemmli97.tenshilib.forge.platform.patreon.PatreonImpl");

    Optional<PatreonPlayerSetting> playerSettings(Player player);

    void sendToClient(ServerPlayer player, ServerPlayer target);

    void sendToTracking(ServerPlayer player, S2CEffectUpdatePkt pkt);
}
