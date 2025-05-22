package io.github.flemmli97.tenshilib.common.item;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.EntityUtil;
import io.github.flemmli97.tenshilib.common.network.NetworkCrossPlat;
import io.github.flemmli97.tenshilib.common.network.S2CAnimationScreen;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class AnimationDebugger extends Item {

    public static final String SAVED_ENTITY = TenshiLib.MODID + ":saved_entity";
    public static final String ANIMATION_IDX = TenshiLib.MODID + ":animation";

    public AnimationDebugger(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (entity instanceof IAnimated) {
                CompoundTag compound = new CompoundTag();
                if (stack.hasTag())
                    compound = stack.getTag();
                compound.putString(SAVED_ENTITY, entity.getStringUUID());
                compound.putInt(ANIMATION_IDX, -1);
                stack.setTag(compound);
                player.setItemInHand(usedHand, stack);
                serverPlayer.sendMessage(new TranslatableComponent("tenshilib.item.animation.select", entity.getName()), ChatType.GAME_INFO, Util.NIL_UUID);
            }
        }
        return InteractionResult.sidedSuccess(player.level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (stack.hasTag() && stack.getTag().contains(SAVED_ENTITY)) {
                Mob storedEntity = EntityUtil.findFromUUID(Mob.class, player.level,
                        UUID.fromString(stack.getTag().getString(SAVED_ENTITY)));
                if (storedEntity instanceof IAnimated animated && storedEntity.isAlive()) {
                    if (player.isShiftKeyDown()) {
                        NetworkCrossPlat.INSTANCE.sendToClient(new S2CAnimationScreen(usedHand, (Mob) animated), serverPlayer);
                    } else {
                        int idx = this.getIndex(stack);
                        if (idx >= 0 && idx < animated.getAnimationHandler().getAnimations().length) {
                            animated.getAnimationHandler().setAnimation(animated.getAnimationHandler().getAnimations()[idx]);
                        }
                    }
                } else {
                    stack.getTag().remove(SAVED_ENTITY);
                    stack.getTag().remove(ANIMATION_IDX);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, player.level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public void updateIndex(ItemStack stack, int idx) {
        CompoundTag compound = new CompoundTag();
        if (stack.hasTag())
            compound = stack.getTag();
        compound.putInt(ANIMATION_IDX, idx);
        stack.setTag(compound);
    }

    public int getIndex(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(ANIMATION_IDX) : -1;
    }
}
