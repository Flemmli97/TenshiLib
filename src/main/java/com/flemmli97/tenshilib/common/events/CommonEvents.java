package com.flemmli97.tenshilib.common.events;

import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import com.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CommonEvents {

    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getPlayer().world.isRemote && event.getPlayer().getHeldItemMainhand().getItem() instanceof IAOEWeapon) {
            AOEWeaponHandler.onAOEWeaponSwing(event.getPlayer(), (IAOEWeapon) event.getPlayer().getHeldItemMainhand().getItem());
        }
    }
}
