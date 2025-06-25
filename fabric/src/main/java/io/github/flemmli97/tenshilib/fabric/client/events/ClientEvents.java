package io.github.flemmli97.tenshilib.fabric.client.events;

import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.util.FastColor;

public class ClientEvents {

    public static void itemColors() {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            ColorProviderRegistry.ITEM.register((stack, i) -> FastColor.ARGB32.opaque(egg.getColor(stack, i)), egg);
    }
}
