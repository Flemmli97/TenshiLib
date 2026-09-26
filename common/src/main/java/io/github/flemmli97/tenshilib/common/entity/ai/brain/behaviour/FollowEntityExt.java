package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;
import net.tslat.smartbrainlib.util.RandomUtil;

/**
 * A better FollowEntity impl
 * Does not try to use surface position but still keeps the predicate for valid positions
 * Also allows tp onto water if applicable
 */
public class FollowEntityExt<E extends PathfinderMob, T extends Entity> extends FollowEntity<E, T> {

    @Override
    protected BlockPos getTeleportPos(E entity, T target, BlockPos targetPos) {
        Level level = entity.level();
        // Disable surface position
        return RandomUtil.getRandomPositionWithinRange(targetPos, 5, 5, 5, 1, 1, 1, false, level, 10, (state, statePos) ->
                this.teleportPredicate.test(entity, statePos, state));
    }

    @Override
    protected boolean isTeleportable(E entity, BlockPos pos, BlockState state) {
        // We don't need to handle aquatic mobs as their pathfinding should solve that aka super impl
        if (entity.getNavigation().canFloat() && state.getFluidState().is(FluidTags.WATER)) {
            if (!entity.level().noCollision(entity, entity.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(entity.position())))) {
                return false;
            }
            BlockState above = entity.level().getBlockState(pos.above());
            return entity.canBreatheUnderwater() || !above.getFluidState().is(FluidTags.WATER);
        }
        return super.isTeleportable(entity, pos, state);
    }
}
