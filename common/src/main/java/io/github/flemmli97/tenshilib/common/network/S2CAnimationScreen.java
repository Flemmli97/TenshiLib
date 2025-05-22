package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class S2CAnimationScreen implements Packet {

    public static final ResourceLocation ID = new ResourceLocation(TenshiLib.MODID, "s2c_animation_screen");

    private final InteractionHand hand;
    private final int entity;

    public S2CAnimationScreen(InteractionHand hand, LivingEntity entity) {
        this.hand = hand;
        this.entity = entity.getId();
    }

    private S2CAnimationScreen(InteractionHand hand, int entity) {
        this.hand = hand;
        this.entity = entity;
    }

    public static S2CAnimationScreen read(FriendlyByteBuf buf) {
        return new S2CAnimationScreen(buf.readEnum(InteractionHand.class), buf.readInt());
    }

    public static void handle(S2CAnimationScreen pkt) {
        Player player = ClientHandlers.clientPlayer();
        if (player == null)
            return;
        Entity entity = player.level.getEntity(pkt.entity);
        if (!(entity instanceof LivingEntity) || !(entity instanceof IAnimated))
            return;
        ClientHandlers.openAnimationGui((LivingEntity & IAnimated) entity, pkt.hand);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeInt(this.entity);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}