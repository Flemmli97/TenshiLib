package io.github.flemmli97.tenshilib.forge.client.events;

import io.github.flemmli97.tenshilib.client.AnimationManager;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.client.CustomRiderRendererManager;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLivingEvent;

public class ClientEvents {

    public static void reloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(AnimationManager.getInstance());
        event.registerReloadListener(CustomRiderRendererManager.getInstance());
    }

    public static void clickSpecial(InputEvent.ClickInputEvent event) {
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND) {
            boolean canceled = ClientHandlers.emptyClick();
            if (canceled) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    public static void itemColors(ColorHandlerEvent.Item event) {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            event.getItemColors().register(egg::getColor, egg);
    }

    public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
        if (ClientHandlers.shouldDisableRender(event.getEntity()))
            event.setCanceled(true);
    }
}
