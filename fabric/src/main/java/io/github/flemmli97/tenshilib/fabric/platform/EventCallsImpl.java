package io.github.flemmli97.tenshilib.fabric.platform;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.EntityBeam;
import io.github.flemmli97.tenshilib.fabric.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.fabric.network.ServerPacketHandler;
import io.github.flemmli97.tenshilib.platform.EventCalls;
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


public class EventCallsImpl implements EventCalls {

    @Override
    public boolean aoeAttackCall(Player player, ItemStack stack, List<Entity> list) {
        return AOEAttackEvent.ATTACK.invoker().call(player, stack, list);
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
    public boolean beamHitCall(EntityBeam beam, HitResult result) {
        return false;
    }

    @Override
    public <T extends Entity & IAnimated> void sendEntityAnimationPacket(T entity) {
        ServerPacketHandler.updateAnimationPkt(entity);
    }
}
