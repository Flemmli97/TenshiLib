package io.github.flemmli97.tenshilib.forge.client.events;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import io.github.flemmli97.tenshilib.client.AnimationManager;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.client.CustomRiderRendererManager;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.common.network.C2SPacketHit;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
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
            Minecraft client = Minecraft.getInstance();
            if (client.hitResult != null && client.hitResult.getType() != HitResult.Type.BLOCK) {
                ItemStack main = client.player.getMainHandItem();
                if (main.getItem() instanceof IExtendedWeapon) {
                    event.setCanceled(true);
                    PacketHandler.sendToServer(new C2SPacketHit(C2SPacketHit.HitType.EXT));
                    client.player.resetAttackStrengthTicker();
                } else if (main.getItem() instanceof IAOEWeapon) {
                    event.setCanceled(true);
                    PacketHandler.sendToServer(new C2SPacketHit(C2SPacketHit.HitType.AOE));
                    client.player.resetAttackStrengthTicker();
                }
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
