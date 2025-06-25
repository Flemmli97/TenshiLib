package io.github.flemmli97.tenshilib.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public interface TenshiLibEventCalls {

    TenshiLibEventCalls INSTANCE = LoaderInitializer.getImplInstance(TenshiLibEventCalls.class,
            "io.github.flemmli97.tenshilib.fabric.loader.TenshiLibEventCallsImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.TenshiLibEventCallsImpl");

    boolean aoeAttackCall(Player player, ItemStack item, List<Entity> list);

    boolean projectileHitCall(Projectile projectile, HitResult result);

    boolean beamHitCall(BeamEntity beam, HitResult result);
}
