package io.github.flemmli97.tenshilib.neoforge.client.events;

import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.client.CustomRiderRendererManager;
import io.github.flemmli97.tenshilib.client.TenshilibShaders;
import io.github.flemmli97.tenshilib.client.data.GeoAnimationManager;
import io.github.flemmli97.tenshilib.client.data.GeoModelManager;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public class ClientEvents {

    public static void reloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(GeoAnimationManager.getInstance());
        event.registerReloadListener(CustomRiderRendererManager.getInstance());
        event.registerReloadListener(GeoModelManager.getInstance());
    }

    public static void clickSpecial(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && event.getHand() == InteractionHand.MAIN_HAND) {
            boolean canceled = ClientHandlers.onClientClick();
            if (canceled) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
    }

    public static void itemColors(RegisterColorHandlersEvent.Item event) {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            event.register((stack, i) -> FastColor.ARGB32.opaque(egg.getColor(stack, i)), egg);
    }

    public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
        if (ClientHandlers.shouldDisableRender(event.getEntity()))
            event.setCanceled(true);
    }

    public static void registerShader(RegisterShadersEvent event) {
        TenshilibShaders.registerShader(((id, vertexFormat, onLoad) ->
                event.registerShader(new ShaderInstance(event.getResourceProvider(), id, vertexFormat), onLoad)));
    }
}
