package io.github.flemmli97.tenshilib;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.entity.EntityBeam;
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class EventCalls {

    @ExpectPlatform
    public static boolean aoeAttackCall(Player player, ItemStack item, List<Entity> list) {
        throw new AssertionError();
    }

    /**
     * Register an handler for this event without depending on platform loader
     */
    @ExpectPlatform
    public static void registerAOEEventHandler(Func3<Player, ItemStack, List<Entity>, Boolean> func) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean playerAttackCall(Player player, Entity target) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Pair<Boolean, Float> criticalAttackCall(Player player, Entity target, boolean crit, float dmgMod) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void destroyItemCall(Player player, ItemStack stack, InteractionHand hand) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean specialSpawnCall(Mob entity, Level world, float x, float y, float z, BaseSpawner spawner, MobSpawnType spawnReason) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean projectileHitCall(Projectile projectile, HitResult result) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean beamHitCall(EntityBeam beam, HitResult result) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends Entity & IAnimated> void sendEntityAnimationPacket(T entity) {
        throw new AssertionError();
    }

    public interface Func3<A, B, C, D> {
        D apply(A a, B b, C c);
    }
}
