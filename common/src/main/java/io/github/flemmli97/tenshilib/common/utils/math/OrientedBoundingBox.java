package io.github.flemmli97.tenshilib.common.utils.math;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Oriented bounding box with collision detection
 * It uses an AABB as the base and rotates it as needed
 * Dimensions of AABB correlates to:
 * X - Width
 * Y - Height
 * Z - Length
 */
public class OrientedBoundingBox {

    private AABB baseBox;
    private AABB outerBox;
    // Rotation in degrees
    private float yRot, xRot;

    private Vec3 offset;
    private final Vec3[] vertices = new Vec3[8];
    private Vec3 axisX, axisY, axisZ;

    public OrientedBoundingBox(AABB box) {
        this(box, 0, 0, Vec3.ZERO);
    }

    public OrientedBoundingBox(AABB box, float yRot, float xRot) {
        this(box, yRot, xRot, Vec3.ZERO);
    }

    public OrientedBoundingBox(AABB box, Vec3 offset) {
        this(box, 0, 0, offset);
    }

    public OrientedBoundingBox(AABB box, float yRot, float xRot, Vec3 offset) {
        this.baseBox = box;
        this.yRot = yRot;
        this.xRot = xRot;
        this.offset = offset;
        this.compute();
    }

    /**
     * Creates an AABB with given dimension that will result
     * in a front facing Hitbox
     */
    public static AABB baseBox(double width, double height, double length) {
        return new AABB(-width * 0.5, -height * 0.5, 0, width * 0.5, height * 0.5, length);
    }

    /**
     * Gets the AABB of the entity based on the origin (Zero)
     * As OBBs require relative AABB
     */
    public static AABB originAABB(Entity entity) {
        return entity.getBoundingBox().move(entity.position().scale(-1));
    }

    public static OrientedBoundingBox fromBuffer(FriendlyByteBuf buf) {
        return new OrientedBoundingBox(new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(), buf.readFloat(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    private static boolean collides(Vec3[] firstBoxVertices, Vec3[] secondBoxVertices, Vec3[] axes) {
        for (Vec3 axis : axes) {
            Projection first = projectOntoAxis(firstBoxVertices, axis);
            Projection second = projectOntoAxis(secondBoxVertices, axis);
            if (!overlap(first, second))
                return false;
        }
        return true;
    }

    private static Projection projectOntoAxis(Vec3[] vertices, Vec3 axis) {
        double min = axis.dot(vertices[0]);
        double max = min;

        for (Vec3 vertex : vertices) {
            double projection = axis.dot(vertex);
            if (projection < min) min = projection;
            if (projection > max) max = projection;
        }

        return new Projection(min, max);
    }

    private static boolean overlap(Projection first, Projection second) {
        return first.max >= second.min && second.max >= first.min;
    }

    public OrientedBoundingBox rotate(float yRot, float xRot) {
        this.yRot = yRot;
        this.xRot = xRot;
        this.compute();
        return this;
    }

    public OrientedBoundingBox move(double x, double y, double z) {
        return this.move(new Vec3(x, y, z));
    }

    public OrientedBoundingBox move(Vec3 offset) {
        this.offset = this.offset.add(offset);
        this.compute();
        return this;
    }

    public OrientedBoundingBox setPos(double x, double y, double z) {
        return this.move(new Vec3(x, y, z));
    }

    public OrientedBoundingBox setPos(Vec3 offset) {
        this.offset = offset;
        this.compute();
        return this;
    }

    public OrientedBoundingBox inflate(double val) {
        return this.inflate(val, val, val);
    }

    public OrientedBoundingBox inflate(double x, double y, double z) {
        this.baseBox = this.baseBox.inflate(x, y, z);
        this.compute();
        return this;
    }

    public OrientedBoundingBox apply(Function<AABB, AABB> func) {
        this.baseBox = func.apply(this.baseBox);
        this.compute();
        return this;
    }

    public boolean intersects(OrientedBoundingBox box) {
        Vec3[] axis = new Vec3[6];
        axis[0] = this.axisX;
        axis[1] = this.axisY;
        axis[2] = this.axisZ;
        axis[3] = box.axisX;
        axis[4] = box.axisY;
        axis[5] = box.axisZ;
        return collides(this.vertices, box.vertices, axis);
    }

    public boolean intersects(AABB aabb) {
        if (this.xRot == 0 && this.yRot == 0) {
            return this.baseBox.move(this.offset).intersects(aabb);
        }
        Vec3[] axis = new Vec3[6];
        axis[0] = this.axisX;
        axis[1] = this.axisY;
        axis[2] = this.axisZ;
        axis[3] = new Vec3(1, 0, 0);
        axis[4] = new Vec3(0, 1, 0);
        axis[5] = new Vec3(0, 0, 1);
        Vec3[] verticesAABB = new Vec3[8];

        verticesAABB[0] = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
        verticesAABB[1] = new Vec3(aabb.maxX, aabb.minY, aabb.minZ);
        verticesAABB[2] = new Vec3(aabb.maxX, aabb.minY, aabb.maxZ);
        verticesAABB[3] = new Vec3(aabb.minX, aabb.minY, aabb.maxZ);

        // Top vertices
        verticesAABB[4] = new Vec3(aabb.minX, aabb.maxY, aabb.minZ);
        verticesAABB[5] = new Vec3(aabb.maxX, aabb.maxY, aabb.minZ);
        verticesAABB[6] = new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ);
        verticesAABB[7] = new Vec3(aabb.minX, aabb.maxY, aabb.maxZ);
        return collides(this.vertices, verticesAABB, axis);
    }

    public <T extends Entity> List<T> intersectingEntities(Level level, LivingEntity entity,
                                                           boolean ignoreBlocks, EntityTypeTest<Entity, T> typeTest, Predicate<T> pred) {
        BlockCollisionDetector collisionDetector = ignoreBlocks ? null : new BlockCollisionDetector(level, this, entity);
        return this.intersectingEntities(level, collisionDetector, typeTest, pred);
    }

    public <T extends Entity> List<T> intersectingEntities(Level level, BlockCollisionDetector collisionDetector, EntityTypeTest<Entity, T> typeTest, Predicate<T> pred) {
        AABB box = this.getEncompassingBox();
        List<T> list = level.getEntities(typeTest, box, pred);
        list.removeIf(e -> !this.intersects(e.getBoundingBox()) || (collisionDetector != null && !collisionDetector.noBlockCollide(e)));
        return list;
    }

    public boolean collidesBlocks(Level level, @Nullable Entity entity) {
        for (VoxelShape shape : level.getBlockCollisions(entity, this.getEncompassingBox())) {
            if (this.intersects(shape.bounds()))
                return true;
        }
        return false;
    }

    public AABB getBaseBox() {
        return this.baseBox;
    }

    /**
     * @return A bounding box that contains this OBB
     */
    public AABB getEncompassingBox() {
        return this.outerBox;
    }

    public float getXRot() {
        return this.xRot;
    }

    public float getYRot() {
        return this.yRot;
    }

    public Vec3[] getVertices() {
        return this.vertices;
    }

    public Vec3 getOffset() {
        return this.offset;
    }

    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeDouble(this.baseBox.maxX);
        buf.writeDouble(this.baseBox.maxY);
        buf.writeDouble(this.baseBox.maxZ);
        buf.writeDouble(this.baseBox.minX);
        buf.writeDouble(this.baseBox.minY);
        buf.writeDouble(this.baseBox.minZ);
        buf.writeFloat(this.yRot);
        buf.writeFloat(this.xRot);
        buf.writeDouble(this.offset.x);
        buf.writeDouble(this.offset.y);
        buf.writeDouble(this.offset.z);
    }

    private void compute() {
        // Bottom vertices
        float xRotRad = Mth.DEG_TO_RAD * this.xRot;
        float yRotRad = -Mth.DEG_TO_RAD * this.yRot;
        this.vertices[0] = new Vec3(this.baseBox.minX, this.baseBox.minY, this.baseBox.minZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);
        this.vertices[1] = new Vec3(this.baseBox.maxX, this.baseBox.minY, this.baseBox.minZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);
        this.vertices[2] = new Vec3(this.baseBox.maxX, this.baseBox.minY, this.baseBox.maxZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);
        this.vertices[3] = new Vec3(this.baseBox.minX, this.baseBox.minY, this.baseBox.maxZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);

        // Top vertices
        this.vertices[4] = new Vec3(this.baseBox.minX, this.baseBox.maxY, this.baseBox.minZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);
        this.vertices[5] = new Vec3(this.baseBox.maxX, this.baseBox.maxY, this.baseBox.minZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);
        this.vertices[6] = new Vec3(this.baseBox.maxX, this.baseBox.maxY, this.baseBox.maxZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);
        this.vertices[7] = new Vec3(this.baseBox.minX, this.baseBox.maxY, this.baseBox.maxZ).xRot(xRotRad)
                .yRot(yRotRad).add(this.offset);

        this.axisX = new Vec3(1, 0, 0).xRot(xRotRad)
                .yRot(yRotRad);
        this.axisY = new Vec3(0, 1, 0).xRot(xRotRad)
                .yRot(yRotRad);
        this.axisZ = new Vec3(0, 0, 1).xRot(xRotRad)
                .yRot(yRotRad);
        double minX = this.vertices[0].x();
        double minY = this.vertices[0].y();
        double minZ = this.vertices[0].z();
        double maxX = this.vertices[0].x();
        double maxY = this.vertices[0].y();
        double maxZ = this.vertices[0].z();
        for (Vec3 vertices : this.vertices) {
            if (vertices.x < minX) {
                minX = vertices.x;
            } else if (vertices.x > maxX) {
                maxX = vertices.x;
            }
            if (vertices.y < minY) {
                minY = vertices.y;
            } else if (vertices.y > maxY) {
                maxY = vertices.y;
            }
            if (vertices.z < minZ) {
                minZ = vertices.z;
            } else if (vertices.z > maxZ) {
                maxZ = vertices.z;
            }
        }
        this.outerBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return String.format("OBB[aabb=%s, yRot=%s, xRot=%s, offset=%s]", this.baseBox, this.yRot, this.xRot, this.offset);
    }

    private record Projection(double min, double max) {

    }

    public static class BlockCollisionDetector {

        private final BlockGetter blockGetter;
        private final Vec3 from;
        private final Entity source;

        public BlockCollisionDetector(Level level, OrientedBoundingBox obb, Entity source) {
            AABB box = obb.getEncompassingBox();
            BlockPos first = BlockPos.containing(box.minX, box.minY, box.minZ);
            BlockPos second = new BlockPos(Mth.ceil(box.maxX), Mth.ceil(box.maxY), Mth.ceil(box.maxZ));
            this.blockGetter = new PathNavigationRegion(level, first, second);
            this.from = obb.getOffset();
            this.source = source;
        }

        /**
         * Check if entity is not behind blocks.
         * Divides the bounding boxes into multiple points to raycast
         * Checks the position and vertices of the bounding box first.
         * If bounding box is large will also divide the edges to check
         */
        public boolean noBlockCollide(Entity entity) {
            AABB aabb = entity.getBoundingBox();
            if (this.blockGetter.clip(new ClipContext(this.from, entity.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                    this.source)).getType() == HitResult.Type.MISS)
                return true;
            // Vertices of AABB
            List<Vec3> points = new ArrayList<>();
            List<Vec3> maxYs = new ArrayList<>();
            points.add(new Vec3(aabb.minX, aabb.minY, aabb.minZ));
            points.add(new Vec3(aabb.maxX, aabb.minY, aabb.minZ));
            points.add(new Vec3(aabb.maxX, aabb.minY, aabb.maxZ));
            points.add(new Vec3(aabb.minX, aabb.minY, aabb.maxZ));

            maxYs.add(new Vec3(aabb.minX, aabb.maxY, aabb.minZ));
            maxYs.add(new Vec3(aabb.maxX, aabb.maxY, aabb.minZ));
            maxYs.add(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ));
            maxYs.add(new Vec3(aabb.minX, aabb.maxY, aabb.maxZ));
            // Check in between
            int xSplit = (int) (entity.getBbWidth() / 0.4);
            if (xSplit > 0) {
                xSplit += 1;
                double xStep = entity.getBbWidth() / xSplit;
                for (int x = 1; x < xSplit; x++) {
                    points.add(new Vec3(aabb.minX + x * xStep, aabb.minY, aabb.minZ));
                    points.add(new Vec3(aabb.minX + x * xStep, aabb.minY, aabb.maxZ));
                    points.add(new Vec3(aabb.minX, aabb.minY, aabb.minZ + x * xStep));
                    points.add(new Vec3(aabb.maxX, aabb.minY, aabb.minZ + x * xStep));

                    maxYs.add(new Vec3(aabb.minX + x * xStep, aabb.maxY, aabb.minZ));
                    maxYs.add(new Vec3(aabb.minX + x * xStep, aabb.maxY, aabb.maxZ));
                    maxYs.add(new Vec3(aabb.minX, aabb.maxY, aabb.minZ + x * xStep));
                    maxYs.add(new Vec3(aabb.maxX, aabb.maxY, aabb.minZ + x * xStep));
                }
            }
            int ySplit = (int) (entity.getBbHeight() / 0.5);
            if (ySplit > 0) {
                ySplit += 1;
                double yStep = entity.getBbHeight() / ySplit;
                List<Vec3> verticals = new ArrayList<>();
                for (int y = 1; y < ySplit; y++) {
                    for (Vec3 current : points) {
                        verticals.add(new Vec3(current.x(), aabb.minY + y * yStep, current.z()));
                    }
                }
                points.addAll(verticals);
            }
            points.addAll(maxYs);
            for (Vec3 point : points) {
                if (this.blockGetter.clip(new ClipContext(this.from, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                        this.source)).getType() == HitResult.Type.MISS)
                    return true;
            }
            return false;
        }
    }
}
