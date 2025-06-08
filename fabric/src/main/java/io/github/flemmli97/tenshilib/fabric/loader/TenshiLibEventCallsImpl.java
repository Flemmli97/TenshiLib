package io.github.flemmli97.tenshilib.fabric.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import io.github.flemmli97.tenshilib.fabric.events.AOEAttackHandlerImpl;
import io.github.flemmli97.tenshilib.loader.TenshiLibEventCalls;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;


public class TenshiLibEventCallsImpl implements TenshiLibEventCalls {

    @Override
    public boolean aoeAttackCall(Player player, ItemStack stack, List<Entity> list) {
        return AOEAttackHandlerImpl.ATTACK.invoker().preventAttack(player, stack, list);
    }

    @Override
    public boolean playerAttackCall(Player player, Entity target) {
        return AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, target, null) != InteractionResult.PASS;
    }

    @Override
    public Pair<Boolean, Float> criticalAttackCall(Player player, Entity target, boolean crit, float dmgMod) {
        return Pair.of(crit, dmgMod);
    }

    @Override
    public void destroyItemCall(Player player, ItemStack stack, InteractionHand hand) {
    }

    @Override
    public boolean projectileHitCall(Projectile projectile, HitResult result) {
        return false;
    }

    @Override
    public boolean beamHitCall(BeamEntity beam, HitResult result) {
        return false;
    }
}
