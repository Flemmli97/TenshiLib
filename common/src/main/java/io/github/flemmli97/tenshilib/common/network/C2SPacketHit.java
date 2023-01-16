package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import io.github.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

public class C2SPacketHit implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "c2s_item_special");

    private final HitType type;

    public C2SPacketHit(HitType type) {
        this.type = type;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.type.ordinal());
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public static C2SPacketHit fromBytes(FriendlyByteBuf buf) {
        return new C2SPacketHit(HitType.values()[buf.readInt()]);
    }

    public static void handlePacket(C2SPacketHit pkt, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (pkt.type == HitType.EXT && stack.getItem() instanceof IExtendedWeapon item) {
            EntityHitResult res = RayTraceUtils.calculateEntityFromLook(player, item.getRange(player, stack));
            if (res != null && res.getEntity() != null)
                player.attack(res.getEntity());
        }
        if (pkt.type == HitType.AOE && stack.getItem() instanceof IAOEWeapon weapon) {
            AOEWeaponHandler.onAOEWeaponSwing(player, stack, weapon);
        }
    }

    public enum HitType {
        EXT, AOE
    }
}