package io.github.flemmli97.tenshilib.platform;

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

public interface EventCalls {

    EventCalls INSTANCE = InitUtil.getPlatformInstance(EventCalls.class,
            "io.github.flemmli97.tenshilib.fabric.platform.EventCallsImpl",
            "io.github.flemmli97.tenshilib.forge.platform.EventCallsImpl");

    boolean aoeAttackCall(Player player, ItemStack item, List<Entity> list);

    boolean playerAttackCall(Player player, Entity target);

    Pair<Boolean, Float> criticalAttackCall(Player player, Entity target, boolean crit, float dmgMod);

    void destroyItemCall(Player player, ItemStack stack, InteractionHand hand);

    boolean specialSpawnCall(Mob entity, Level world, float x, float y, float z, BaseSpawner spawner, MobSpawnType spawnReason);

    boolean projectileHitCall(Projectile projectile, HitResult result);

    boolean beamHitCall(EntityBeam beam, HitResult result);

    <T extends Entity & IAnimated> void sendEntityAnimationPacket(T entity);

    interface Func3<A, B, C, D> {
        D apply(A a, B b, C c);
    }
}
