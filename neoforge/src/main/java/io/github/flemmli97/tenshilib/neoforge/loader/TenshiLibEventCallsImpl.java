package io.github.flemmli97.tenshilib.neoforge.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import io.github.flemmli97.tenshilib.loader.TenshiLibEventCalls;
import io.github.flemmli97.tenshilib.neoforge.events.AOEAttackEventForge;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TenshiLibEventCallsImpl implements TenshiLibEventCalls {

    @Override
    public boolean aoeAttackCall(Player player, ItemStack stack, List<Entity> list) {
        return NeoForge.EVENT_BUS.post(new AOEAttackEventForge(player, stack, list)).isCanceled();
    }

    @Override
    public boolean playerAttackCall(Player player, Entity target) {
        return CommonHooks.onPlayerAttackTarget(player, target);
    }

    @Override
    public Pair<Boolean, Float> criticalAttackCall(Player player, Entity target, boolean crit, float dmgMod) {
        CriticalHitEvent hitResult = CommonHooks.fireCriticalHit(player, target, crit, dmgMod);
        return Pair.of(hitResult.isCriticalHit(), hitResult.getDamageMultiplier());
    }

    @Override
    public void destroyItemCall(Player player, ItemStack stack, InteractionHand hand) {
        EventHooks.onPlayerDestroyItem(player, stack, hand);
    }

    @Override
    public boolean projectileHitCall(Projectile projectile, HitResult result) {
        return EventHooks.onProjectileImpact(projectile, result);
    }

    @Override
    public boolean beamHitCall(BeamEntity beam, HitResult result) {
        return false;
    }
}
