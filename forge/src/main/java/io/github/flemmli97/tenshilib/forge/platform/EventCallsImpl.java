package io.github.flemmli97.tenshilib.forge.platform;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.EntityBeam;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.forge.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class EventCallsImpl implements EventCalls {

    @Override
    public boolean aoeAttackCall(Player player, ItemStack stack, List<Entity> list) {
        return MinecraftForge.EVENT_BUS.post(new AOEAttackEvent(player, stack, list));
    }

    @Override
    public boolean playerAttackCall(Player player, Entity target) {
        return ForgeHooks.onPlayerAttackTarget(player, target);
    }

    @Override
    public Pair<Boolean, Float> criticalAttackCall(Player player, Entity target, boolean crit, float dmgMod) {
        CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, target, crit, dmgMod);
        return Pair.of(hitResult != null, hitResult == null ? dmgMod : hitResult.getDamageModifier());
    }

    @Override
    public void destroyItemCall(Player player, ItemStack stack, InteractionHand hand) {
        ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
    }

    @Override
    public boolean projectileHitCall(Projectile projectile, HitResult result) {
        return ForgeEventFactory.onProjectileImpact(projectile, result);
    }

    @Override
    public boolean beamHitCall(EntityBeam beam, HitResult result) {
        return false;
    }

    @Override
    public <T extends Entity & IAnimated> void sendEntityAnimationPacket(T entity) {
        PacketHandler.sendToTracking(S2CEntityAnimation.create(entity), entity);
    }
}
