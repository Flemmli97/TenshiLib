package io.github.flemmli97.tenshilib.common.item;

import io.github.flemmli97.tenshilib.common.entity.EntityUtils;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.network.S2CAnimationScreen;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
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
import java.util.function.Supplier;

public class AnimationDebugger extends Item {

    private final Supplier<DataComponentType<UUID>> entityIDType;
    private final Supplier<DataComponentType<String>> animationIdx;

    public AnimationDebugger(Properties props, Supplier<DataComponentType<UUID>> entityIDType, Supplier<DataComponentType<String>> animationIdx) {
        super(props);
        this.entityIDType = entityIDType;
        this.animationIdx = animationIdx;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (entity instanceof AnimatedEntity) {
                stack.set(this.entityIDType.get(), entity.getUUID());
                stack.remove(this.animationIdx.get());
                player.setItemInHand(usedHand, stack);
                serverPlayer.displayClientMessage(Component.translatable("tenshilib.item.animation.select", entity.getName()), true);
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player instanceof ServerPlayer serverPlayer) {
            UUID uuid = stack.get(this.entityIDType.get());
            if (uuid != null) {
                Mob storedEntity = EntityUtils.findFromUUID(Mob.class, player.level(), uuid);
                if (storedEntity instanceof AnimatedEntity animated && storedEntity.isAlive()) {
                    if (player.isShiftKeyDown()) {
                        LoaderNetwork.INSTANCE.sendToPlayer(new S2CAnimationScreen(usedHand, (Mob) animated), serverPlayer);
                    } else {
                        String id = this.getId(stack);
                        if (!id.isEmpty()) {
                            animated.getAnimationHandler().setAnimation(animated.getAnimationHandler().getAnimations().get(id));
                        }
                    }
                } else {
                    stack.remove(this.entityIDType.get());
                    stack.remove(this.animationIdx.get());
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, player.level().isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public void updateId(ItemStack stack, String id) {
        stack.set(this.animationIdx.get(), id);
    }

    public String getId(ItemStack stack) {
        return stack.getOrDefault(this.animationIdx.get(), "");
    }
}
