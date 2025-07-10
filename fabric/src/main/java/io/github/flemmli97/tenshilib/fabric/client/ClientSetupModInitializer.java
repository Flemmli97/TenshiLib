package io.github.flemmli97.tenshilib.fabric.client;

/**
 * Use this instead of {@link net.fabricmc.api.ClientModInitializer} as this runs after all registry contents have been registered.
 * To define this entrypoint in your {@code fabric.mod.json} use the key {@code tenshilib_client}
 */
public interface ClientSetupModInitializer {

    /**
     * Run the client setups for the mod
     */
    void clientSetup();
}
