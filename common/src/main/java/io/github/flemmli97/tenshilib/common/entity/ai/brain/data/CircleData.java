package io.github.flemmli97.tenshilib.common.entity.ai.brain.data;

import net.minecraft.world.entity.ai.behavior.PositionTracker;

public record CircleData(PositionTracker position, boolean clockWise, float radius, float speed) {

}
