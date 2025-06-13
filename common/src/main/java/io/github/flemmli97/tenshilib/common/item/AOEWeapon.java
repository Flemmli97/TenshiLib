package io.github.flemmli97.tenshilib.common.item;

import io.github.flemmli97.tenshilib.common.utils.HitResultUtils;
import io.github.flemmli97.tenshilib.common.utils.math.OrientedBoundingBox;
import io.github.flemmli97.tenshilib.loader.TenshiLibEventCalls;
import io.github.flemmli97.tenshilib.mixinhelper.PlayerAttackAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Items with modified attack range and aoe.
 */
public interface AOEWeapon extends ExtendedWeapon {

    static OrientedBoundingBox createOBB(LivingEntity entity, double length, double width, double height) {
        AABB aabb = new AABB(-width * 0.5, 0, 0, width * 0.5, height, length);
        float yRot = entity.getYRot();
        float xRot = -entity.getXRot();
        return new OrientedBoundingBox(aabb
                .move(0, -aabb.getYsize() * 0.5, 0), yRot, xRot, entity.getEyePosition());
    }

    @Override
    default void executeAttack(Player player, ItemStack stack) {
        if (player.level().isClientSide)
            return;
        OrientedBoundingBox obb = this.attackOBB(player, stack, true);
        List<Entity> list;
        if (obb == null) {
            list = new ArrayList<>();
            EntityHitResult hit = HitResultUtils.calculateEntityFromLook(player, this.getRange(player, stack));
            if (hit != null)
                list.add(hit.getEntity());
        } else {
            list = HitResultUtils.getEntities(player, obb, EntityTypeTest.forClass(Entity.class), null);
        }
        if (TenshiLibEventCalls.INSTANCE.aoeAttackCall(player, stack, list) || list.isEmpty())
            return;
        ((PlayerAttackAccess) player).tenshilib$SetNoSweeping(!this.doSweepingAttack());
        for (int i = 0; i < list.size(); i++) {
            boolean resetCooldown = i == (list.size() - 1);
            ((PlayerAttackAccess) player).tenshilib$SetNoStrengthResetState(!resetCooldown);
            player.attack(list.get(i));
        }
        ((PlayerAttackAccess) player).tenshilib$SetNoStrengthResetState(false);
        ((PlayerAttackAccess) player).tenshilib$SetNoSweeping(false);
    }

    default OrientedBoundingBox attackOBB(LivingEntity entity, ItemStack stack, boolean debug) {
        double width = this.getWidth(entity, stack);
        double length = this.getRange(entity, stack);
        double height = 1;
        return createOBB(entity, width, length, height);
    }

    default float getWidth(LivingEntity entity, ItemStack stack) {
        return 0.5f;
    }
}
