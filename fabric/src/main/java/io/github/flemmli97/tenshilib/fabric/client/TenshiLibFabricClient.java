package io.github.flemmli97.tenshilib.fabric.client;

import io.github.flemmli97.tenshilib.fabric.client.events.ClientEvents;
import io.github.flemmli97.tenshilib.fabric.network.ClientPacketHandler;
import io.github.flemmli97.tenshilib.fabric.platform.patreon.ClientPatreonImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class TenshiLibFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPacketHandler.register();
        ClientEvents.itemColors();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new AnimReloader());
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new RiderLayerReloader());
        ClientPatreonImpl.setup();
    }
}
