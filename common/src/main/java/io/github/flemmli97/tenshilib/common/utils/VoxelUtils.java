package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class VoxelUtils {

    /**
     * Creates and array of Voxelshapes rotated accordingly
     *
     * @return Voxelshape array with directions in order of the 2d indices defined in {@link Direction}
     */
    public static VoxelShape[] joinedOrDirs(ShapeBuilder... shapes) {
        return new VoxelShape[]{
                joinedOr(Direction.from2DDataValue(0), shapes),
                joinedOr(Direction.from2DDataValue(1), shapes),
                joinedOr(Direction.from2DDataValue(2), shapes),
                joinedOr(Direction.from2DDataValue(3), shapes)
        };
    }

    /**
     * Creates an Voxelshape rotated accordingly to the given direction.
     * The default direction is NORTH with no rotation
     */
    public static VoxelShape joinedOr(Direction direction, ShapeBuilder... shapes) {
        return Stream.of(shapes)
                .map(s -> {
                    switch (direction) {
                        case EAST -> {
                            return Block.box(16 - s.z2, s.y1, s.x1, 16 - s.z1, s.y2, s.x2);
                        }
                        case SOUTH -> {
                            return Block.box(16 - s.x2, s.y1, 16 - s.z2, 16 - s.x1, s.y2, 16 - s.z1);
                        }
                        case WEST -> {
                            return Block.box(s.z1, s.y1, 16 - s.x2, s.z2, s.y2, 16 - s.x1);
                        }
                    }
                    return Block.box(s.x1, s.y1, s.z1, s.x2, s.y2, s.z2);
                })
                .reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    }

    public record ShapeBuilder(double x1, double y1, double z1, double x2, double y2, double z2) {
        public static ShapeBuilder of(double x1, double y1, double z1, double x2, double y2, double z2) {
            return new ShapeBuilder(x1, y1, z1, x2, y2, z2);
        }
    }
}
