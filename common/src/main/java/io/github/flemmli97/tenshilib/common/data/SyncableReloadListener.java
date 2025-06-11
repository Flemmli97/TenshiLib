package io.github.flemmli97.tenshilib.common.data;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public interface SyncableReloadListener {

    void onSync(Collection<ServerPlayer> players);

}
