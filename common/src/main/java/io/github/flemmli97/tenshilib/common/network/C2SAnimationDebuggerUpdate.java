package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.item.AnimationDebugger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class C2SAnimationDebuggerUpdate implements CustomPacketPayload {

    public static final Type<C2SAnimationDebuggerUpdate> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "c2s_animation_debugger_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAnimationDebuggerUpdate> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public C2SAnimationDebuggerUpdate decode(RegistryFriendlyByteBuf buf) {
            return new C2SAnimationDebuggerUpdate(buf.readEnum(InteractionHand.class), buf.readUtf());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, C2SAnimationDebuggerUpdate pkt) {
            buf.writeEnum(pkt.hand);
            buf.writeUtf(pkt.id);
        }
    };

    private final InteractionHand hand;
    private final String id;

    public C2SAnimationDebuggerUpdate(InteractionHand hand, String id) {
        this.hand = hand;
        this.id = id;
    }

    public static void handle(C2SAnimationDebuggerUpdate pkt, ServerPlayer sender) {
        ItemStack stack = sender.getItemInHand(pkt.hand);
        if (stack.getItem() instanceof AnimationDebugger debug) {
            debug.updateId(stack, pkt.id);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
