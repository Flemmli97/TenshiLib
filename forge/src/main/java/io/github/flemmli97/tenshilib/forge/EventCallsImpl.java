package io.github.flemmli97.tenshilib.forge;

import io.github.flemmli97.tenshilib.EventCalls;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.EntityBeam;
import io.github.flemmli97.tenshilib.forge.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import io.github.flemmli97.tenshilib.forge.network.S2CEntityAnimation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Consumer;

public class EventCallsImpl {

    public static boolean aoeAttackCall(Player player, ItemStack stack, List<Entity> list) {
        return MinecraftForge.EVENT_BUS.post(new AOEAttackEvent(player, stack, list));
    }

    public static void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func) {
        Consumer<AOEAttackEvent> cons = event -> {
            if (!func.apply(event.getPlayer(), event.usedItem, event.attackList()))
                event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(cons);
    }

    public static boolean playerAttackCall(Player player, Entity target) {
        return ForgeHooks.onPlayerAttackTarget(player, target);
    }

    public static Pair<Boolean, Float> criticalAttackCall(Player player, Entity target, boolean crit, float dmgMod) {
        CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, target, crit, dmgMod);
        return Pair.of(hitResult != null, hitResult == null ? dmgMod : hitResult.getDamageModifier());
    }

    public static void destroyItemCall(Player player, ItemStack stack, InteractionHand hand) {
        ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
    }

    public static boolean specialSpawnCall(Mob entity, Level world, float x, float y, float z, BaseSpawner spawner, MobSpawnType spawnReason) {
        return ForgeEventFactory.doSpecialSpawn(entity, world, x, y, z, spawner, spawnReason);
    }

    public static boolean projectileHitCall(Projectile projectile, HitResult result) {
        return ForgeEventFactory.onProjectileImpact(projectile, result);
    }

    public static boolean beamHitCall(EntityBeam beam, HitResult result) {
        return false;
    }

    public static <T extends Entity & IAnimated> void sendEntityAnimationPacket(T entity) {
        PacketHandler.sendToTracking(S2CEntityAnimation.create(entity), entity);
    }
}
