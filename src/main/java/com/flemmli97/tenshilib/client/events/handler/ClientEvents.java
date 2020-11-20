package com.flemmli97.tenshilib.client.events.handler;

import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import com.flemmli97.tenshilib.api.item.IExtendedWeapon;
import com.flemmli97.tenshilib.common.item.SpawnEgg;
import com.flemmli97.tenshilib.common.network.C2SPacketHit;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

    @SubscribeEvent
    public static void clickSpecial(InputEvent.ClickInputEvent event) {
        if (event.isAttack() && event.getHand() == Hand.MAIN_HAND) {
            Minecraft client = Minecraft.getInstance();
            if (client.objectMouseOver != null && client.objectMouseOver.getType() != RayTraceResult.Type.BLOCK) {
                ItemStack main = client.player.getHeldItemMainhand();
                if (main.getItem() instanceof IExtendedWeapon) {
                    event.setCanceled(true);
                    PacketHandler.sendToServer(new C2SPacketHit(C2SPacketHit.HitType.EXT));
                } else if (main.getItem() instanceof IAOEWeapon) {
                    event.setCanceled(true);
                    PacketHandler.sendToServer(new C2SPacketHit(C2SPacketHit.HitType.AOE));
                }
            }
        }
    }

    @SubscribeEvent
    public static void itemColors(ColorHandlerEvent.Item event) {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            event.getItemColors().register((stack, i) -> egg.getColor(i), egg);
    }
}
