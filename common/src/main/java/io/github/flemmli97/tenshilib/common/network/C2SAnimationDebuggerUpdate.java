package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.item.AnimationDebugger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class C2SAnimationDebuggerUpdate implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "c2s_animation_debugger_update");

    private final InteractionHand hand;
    private final int index;

    public C2SAnimationDebuggerUpdate(InteractionHand hand, int index) {
        this.hand = hand;
        this.index = index;
    }

    public static C2SAnimationDebuggerUpdate read(FriendlyByteBuf buf) {
        return new C2SAnimationDebuggerUpdate(buf.readEnum(InteractionHand.class), buf.readInt());
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
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeInt(this.index);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
