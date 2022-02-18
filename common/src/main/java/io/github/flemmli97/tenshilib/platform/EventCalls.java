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

public abstract class EventCalls {

    protected static EventCalls INSTANCE;

    public static EventCalls instance() {
        return INSTANCE;
    }

    public abstract boolean aoeAttackCall(Player player, ItemStack item, List<Entity> list);

    public abstract boolean playerAttackCall(Player player, Entity target);

    public abstract Pair<Boolean, Float> criticalAttackCall(Player player, Entity target, boolean crit, float dmgMod);

    public abstract void destroyItemCall(Player player, ItemStack stack, InteractionHand hand);

    public abstract boolean specialSpawnCall(Mob entity, Level world, float x, float y, float z, BaseSpawner spawner, MobSpawnType spawnReason);

    public abstract boolean projectileHitCall(Projectile projectile, HitResult result);

    public abstract boolean beamHitCall(EntityBeam beam, HitResult result);

    public abstract <T extends Entity & IAnimated> void sendEntityAnimationPacket(T entity);

    public interface Func3<A, B, C, D> {
        D apply(A a, B b, C c);
    }
}
