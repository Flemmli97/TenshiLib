package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import io.github.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

public class C2SPacketHit implements CustomPacketPayload {

    public static final Type<C2SPacketHit> TYPE = new Type<>(new ResourceLocation(TenshiLib.MODID, "c2s_item_special"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPacketHit> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public C2SPacketHit decode(RegistryFriendlyByteBuf buf) {
            return new C2SPacketHit(buf.readEnum(HitType.class));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, C2SPacketHit pkt) {
            buf.writeEnum(pkt.type);
        }
    };

    private final HitType type;

    public C2SPacketHit(HitType type) {
        this.type = type;
    }

    public static void handlePacket(C2SPacketHit pkt, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (pkt.type == HitType.EXT && stack.getItem() instanceof IExtendedWeapon item && item.onServerSwing(player, stack)) {
            EntityHitResult res = RayTraceUtils.calculateEntityFromLook(player, item.getRange(player, stack));
            if (res != null && res.getEntity() != null && item.onHit(player, stack, res.getEntity()))
                player.attack(res.getEntity());
        }
        if (pkt.type == HitType.AOE && stack.getItem() instanceof IAOEWeapon weapon && weapon.onServerSwing(player, stack)) {
            AOEWeaponHandler.onAOEWeaponSwing(player, stack, weapon);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum HitType {
        EXT, AOE
    }
}