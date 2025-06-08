package io.github.flemmli97.tenshilib.patreon;

import io.github.flemmli97.tenshilib.loader.LoaderInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface TenshiLibPatreonPlatform {

    TenshiLibPatreonPlatform INSTANCE = LoaderInitializer.getImplInstance(TenshiLibPatreonPlatform.class,
            "io.github.flemmli97.tenshilib.fabric.loader.patreon.TenshiLibPatreonImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.patreon.TenshiLibPatreonImpl");

    PatreonPlayerSetting playerSettings(Player player);

    void sendToClient(ServerPlayer player, ServerPlayer target);
}
