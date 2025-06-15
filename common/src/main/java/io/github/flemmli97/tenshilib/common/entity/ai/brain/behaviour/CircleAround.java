package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.CircleData;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.memory.MoreMemoryModules;
import io.github.flemmli97.tenshilib.common.utils.math.MathUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CircleAround<E extends Mob> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORIES = MemoryTest.builder(1).hasMemory(MoreMemoryModules.CIRCLE_DATA.get());

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return this.verifyTracker(entity);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return this.verifyTracker(entity);
    }

    @Override
    protected void tick(E entity) {
        BrainUtils.withMemory(entity, MoreMemoryModules.CIRCLE_DATA.get(), data -> this.circleAround(entity, data));
    }

    protected boolean verifyTracker(E entity) {
        CircleData center = BrainUtils.getMemory(entity, MoreMemoryModules.CIRCLE_DATA.get());
        if (center == null || (center.position() instanceof EntityTracker entityTracker && !entityTracker.getEntity().isAlive())) {
            if (center == null)
                BrainUtils.clearMemory(entity, MoreMemoryModules.CIRCLE_DATA.get());
            return false;
        }
        return true;
    }

    public void circleAround(E entity, CircleData data) {
        Vec3 pos = data.position().currentPosition();
        double posX = pos.x();
        double posZ = pos.z();
        float radius = data.radius();
        double x = entity.getX() - posX;
        double z = entity.getZ() - posZ;
        double r = x * x + z * z;
        if (r < (radius - 1.5) * (radius - 1.5) || r > (radius + 1.5) * (radius + 1.5)) {
            double[] c = MathUtils.closestOnCircle(posX, posZ, entity.getX(), entity.getZ(), radius);
            entity.getNavigation().moveTo(c[0], entity.getY(), c[1], data.speed());
        } else {
            double angle = MathUtils.phiFromPoint(posX, posZ, entity.getX(), entity.getZ()) + (data.clockWise() ? 15 * Mth.DEG_TO_RAD : -15 * Mth.DEG_TO_RAD);
            double nPosX = radius * Math.cos(angle);
            double nPosZ = radius * Math.sin(angle);
            entity.getNavigation().moveTo(posX + nPosX, entity.getY(), posZ + nPosZ, data.speed());
        }
    }
}
