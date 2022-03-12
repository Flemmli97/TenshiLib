package io.github.flemmli97.tenshilib.fabric.client.events;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.fabric.network.ClientPacketHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
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
                return true;
            } else if (main.getItem() instanceof IAOEWeapon) {
                ClientPacketHandler.sendWeaponHitPkt(true);
                client.player.resetAttackStrengthTicker();
                return true;
            }
        }
        return false;
    }

    public static void itemColors() {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            ColorProviderRegistry.ITEM.register((stack, i) -> egg.getColor(i), egg);
    }
}
