package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.item.ExtendedWeapon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class C2SAttackPacket implements CustomPacketPayload {

    public static final Type<C2SAttackPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "c2s_item_special"));
    public static final C2SAttackPacket INSTANCE = new C2SAttackPacket();
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAttackPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private C2SAttackPacket() {
    }

    public static void handle(C2SAttackPacket pkt, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ExtendedWeapon item) {
            item.executeAttack(player, stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}