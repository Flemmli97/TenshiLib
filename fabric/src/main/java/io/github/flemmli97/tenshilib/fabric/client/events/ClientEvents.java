package io.github.flemmli97.tenshilib.fabric.client.events;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.fabric.network.ClientPacketHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class ClientEvents {

    public static boolean clickSpecial() {
        Minecraft client = Minecraft.getInstance();
        if (client.hitResult != null && client.hitResult.getType() != HitResult.Type.BLOCK) {
            ItemStack main = client.player.getMainHandItem();
            if (main.getItem() instanceof IExtendedWeapon) {
                ClientPacketHandler.sendWeaponHitPkt(false);
                client.player.resetAttackStrengthTicker();
                client.player.swing(InteractionHand.MAIN_HAND);
                return true;
            } else if (main.getItem() instanceof IAOEWeapon) {
                ClientPacketHandler.sendWeaponHitPkt(true);
                client.player.resetAttackStrengthTicker();
                client.player.swing(InteractionHand.MAIN_HAND);
                return true;
            }
        }
        return false;
    }

    public static void itemColors() {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            ColorProviderRegistry.ITEM.register(egg::getColor, egg);
    }
}
