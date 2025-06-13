package io.github.flemmli97.tenshilib.common.item;

import io.github.flemmli97.tenshilib.common.network.C2SAttackPacket;
import io.github.flemmli97.tenshilib.common.utils.HitResultUtils;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import io.github.flemmli97.tenshilib.mixinhelper.PlayerAttackAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public interface ExtendedWeapon {

    default void executeAttack(Player player, ItemStack stack) {
        double range = this.getRange(player, stack);
        EntityHitResult hit = HitResultUtils.calculateEntityFromLook(player, range);
        Entity target = hit != null ? hit.getEntity() : null;
        if (target != null) {
            ((PlayerAttackAccess) player).tenshilib$SetNoSweeping(!this.doSweepingAttack());
            player.attack(target);
            ((PlayerAttackAccess) player).tenshilib$SetNoSweeping(false);
        }
        if (this.shouldSwingWeapon(player, stack))
            player.swing(InteractionHand.MAIN_HAND);
    }

    /**
     * This gets called whenever the player tries to attack in {@link net.minecraft.client.Minecraft#startAttack()}
     *
     * @return Return true to prevent the player from doing the action
     */
    default boolean onClientStartAttack(LivingEntity entity, ItemStack stack, HitResult hitResult) {
        switch (hitResult.getType()) {
            case ENTITY, MISS -> {
                LoaderNetwork.INSTANCE.sendToServer(C2SAttackPacket.INSTANCE);
                return true;
            }
            case BLOCK -> {
                if (this.attackOnBlock(entity, stack)) {
                    LoaderNetwork.INSTANCE.sendToServer(C2SAttackPacket.INSTANCE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * If true will try to attack even when targeting a block
     */
    default boolean attackOnBlock(LivingEntity entity, ItemStack stack) {
        return false;
    }

    default boolean shouldSwingWeapon(LivingEntity entity, ItemStack stack) {
        return true;
    }

    default boolean doSweepingAttack() {
        return true;
    }

    default double getRange(LivingEntity entity, ItemStack stack) {
        AttributeInstance inst = entity.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        return inst != null ? inst.getValue() : Attributes.ENTITY_INTERACTION_RANGE.value().getDefaultValue();
    }
}
