package io.github.flemmli97.tenshilib.forge.network;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import io.github.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SPacketHit {

    private final HitType type;

    public C2SPacketHit(HitType type) {
        this.type = type;
    }

    public static C2SPacketHit fromBytes(FriendlyByteBuf buf) {
        return new C2SPacketHit(HitType.values()[buf.readInt()]);
    }

    public static void toBytes(C2SPacketHit pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.type.ordinal());
    }

    public static void handlePacket(C2SPacketHit pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player == null)
                return;
            ItemStack stack = player.getMainHandItem();
            if (pkt.type == HitType.EXT && stack.getItem() instanceof IExtendedWeapon item) {
                EntityHitResult res = RayTraceUtils.calculateEntityFromLook(player, item.getRange());
                if (res != null && res.getEntity() != null)
                    player.attack(res.getEntity());
            }
            if (pkt.type == HitType.AOE && stack.getItem() instanceof IAOEWeapon weapon) {
                AOEWeaponHandler.onAOEWeaponSwing(player, stack, weapon);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum HitType {
        EXT, AOE
    }
}