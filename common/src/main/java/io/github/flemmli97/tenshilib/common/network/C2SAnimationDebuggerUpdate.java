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
            return new C2SAnimationDebuggerUpdate(buf.readEnum(InteractionHand.class), buf.readInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, C2SAnimationDebuggerUpdate pkt) {
            buf.writeEnum(pkt.hand);
            buf.writeInt(pkt.index);
        }
    };

    private final InteractionHand hand;
    private final int index;

    public C2SAnimationDebuggerUpdate(InteractionHand hand, int index) {
        this.hand = hand;
        this.index = index;
    }

    public static void handle(C2SAnimationDebuggerUpdate pkt, ServerPlayer sender) {
        if (sender != null) {
            ItemStack stack = sender.getItemInHand(pkt.hand);
            if (stack.getItem() instanceof AnimationDebugger debug) {
                debug.updateIndex(stack, pkt.index);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
