package com.flemmli97.tenshilib.common.network;

import com.flemmli97.tenshilib.api.item.IAOEWeapon;
import com.flemmli97.tenshilib.api.item.IExtendedWeapon;
import com.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import com.flemmli97.tenshilib.common.utils.RayTraceUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPacketHit {

    private final HitType type;

    public C2SPacketHit(HitType type) {
        this.type = type;
    }

    public static C2SPacketHit fromBytes(PacketBuffer buf) {
        return new C2SPacketHit(HitType.values()[buf.readInt()]);
    }

    public static void toBytes(C2SPacketHit pkt, PacketBuffer buf) {
        buf.writeInt(pkt.type.ordinal());
    }

    public static void handlePacket(C2SPacketHit pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;
            ItemStack stack = player.getHeldItemMainhand();
            if (pkt.type == HitType.EXT && stack.getItem() instanceof IExtendedWeapon) {
                IExtendedWeapon item = (IExtendedWeapon) stack.getItem();
                EntityRayTraceResult res = RayTraceUtils.calculateEntityFromLook(player, item.getRange());
                if (res != null && res.getEntity() != null)
                    player.attackTargetEntityWithCurrentItem(res.getEntity());
            }
            if (pkt.type == HitType.AOE && stack.getItem() instanceof IAOEWeapon) {
                AOEWeaponHandler.onAOEWeaponSwing(player, (IAOEWeapon) stack.getItem());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum HitType {
        EXT, AOE
    }
}