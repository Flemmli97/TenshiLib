package io.github.flemmli97.tenshilib.fabric.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import io.github.flemmli97.tenshilib.fabric.events.AOEAttackHandlerImpl;
import io.github.flemmli97.tenshilib.loader.TenshiLibEventCalls;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class TenshiLibEventCallsImpl implements TenshiLibEventCalls {

    @Override
    public boolean aoeAttackCall(Player player, ItemStack stack, List<Entity> list) {
        return AOEAttackHandlerImpl.ATTACK.invoker().preventAttack(player, stack, list);
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
