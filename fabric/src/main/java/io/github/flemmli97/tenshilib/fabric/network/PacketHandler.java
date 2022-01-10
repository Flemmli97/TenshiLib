package io.github.flemmli97.tenshilib.fabric.network;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import io.github.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Optional;

public class PacketHandler {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(PacketID.hitPacket, PacketHandler::weaponSwing);
    }

    private static void weaponSwing(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        if (player == null)
            return;
        ItemStack stack = player.getMainHandItem();
        boolean isAOE = buf.readBoolean();
        if (!isAOE && stack.getItem() instanceof IExtendedWeapon item) {
            EntityHitResult res = RayTraceUtils.calculateEntityFromLook(player, item.getRange());
            if (res != null && res.getEntity() != null)
                player.attack(res.getEntity());
        }
        if (isAOE && stack.getItem() instanceof IAOEWeapon weapon) {
            AOEWeaponHandler.onAOEWeaponSwing(player, stack, weapon);
        }
    }

    public static <T extends Entity & IAnimated> void updateAnimationPkt(T entity) {
        if (entity.getLevel().isClientSide)
            return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entity.getId());
        buf.writeInt(Optional.ofNullable(entity.getAnimationHandler().getAnimation())
                .map(anim -> {
                    if (anim == AnimatedAction.vanillaAttack)
                        return -1;
                    else {
                        int i = 0;
                        for (AnimatedAction a : entity.getAnimationHandler().getAnimations()) {
                            if (a.getID().equals(anim.getID()))
                                break;
                            i++;
                        }
                        if (i < entity.getAnimationHandler().getAnimations().length)
                            return i;
                        return -2;
                    }
                }).orElse(-2));
        PlayerLookup.tracking(entity)
                .forEach(player -> ServerPlayNetworking.send(player, PacketID.animationPacket, buf));
    }
}
