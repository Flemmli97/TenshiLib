package io.github.flemmli97.tenshilib.fabric.client.events;

import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.common.network.C2SPacketHit;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class ClientEvents {

    public static boolean clickSpecial() {
        return ClientHandlers.emptyClick(isAOE -> {
            C2SPacketHit pkt = new C2SPacketHit(isAOE ? C2SPacketHit.HitType.AOE : C2SPacketHit.HitType.EXT);
            ClientPlayNetworking.send(pkt);
        });
    }

    public static void itemColors() {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            ColorProviderRegistry.ITEM.register(egg::getColor, egg);
    }
}
