package io.github.flemmli97.tenshilib.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple map containing reload listeners that should be synced to clients.
 * Sync happens when a player joins the server or the server triggers a reload
 */
public class SyncedReloadListeners {

    private static final Map<ResourceLocation, SyncableReloadListener> LISTENERS = new HashMap<>();

    public static synchronized void addOrUpdate(ResourceLocation id, SyncableReloadListener syncable) {
        LISTENERS.put(id, syncable);
    }

    public static void triggerSync(Collection<ServerPlayer> players) {
        LISTENERS.values().forEach(l -> l.onSync(players));
    }
}
