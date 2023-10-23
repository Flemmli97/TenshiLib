package io.github.flemmli97.tenshilib.forge.client.events;

import io.github.flemmli97.tenshilib.client.AnimationManager;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.client.CustomRiderRendererManager;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.common.network.C2SPacketHit;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderLivingEvent;

public class ClientEvents {

    public static void reloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(AnimationManager.getInstance());
        event.registerReloadListener(CustomRiderRendererManager.getInstance());
    }

    public static void clickSpecial(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND) {
            boolean canceled = ClientHandlers.emptyClick(aoe -> PacketHandler.sendToServer(new C2SPacketHit(aoe ? C2SPacketHit.HitType.AOE : C2SPacketHit.HitType.EXT)));
            if (canceled) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    public static void itemColors(RegisterColorHandlersEvent.Item event) {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            event.register(egg::getColor, egg);
    }

    public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
        if (ClientHandlers.shouldDisableRender(event.getEntity()))
            event.setCanceled(true);
    }
}
