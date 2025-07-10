package io.github.flemmli97.tenshilib.common.utils.math;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {

    public static final Vec3 NORMAL_X = new Vec3(1, 0, 0);
    public static final Vec3 NORMAL_Y = new Vec3(0, 1, 0);
    public static final Vec3 NORMAL_Z = new Vec3(0, 0, 1);

    /**
     * Gets closest point on circle circumfence from point
     */
    public static double[] closestOnCircle(double centerX, double centerY, double pointX, double pointY, double radius) {
        double x = pointX - centerX;
        double y = pointY - centerY;
        double d0 = Math.sqrt(x * x + y * y);
        return new double[]{x / d0 * radius + centerX, y / d0 * radius + centerY};
    }

    public static List<float[]> pointsOfCircle(float radius, int density) {
        float rad = density * Mth.DEG_TO_RAD;
        float i = -rad;
        List<float[]> list = new ArrayList<>();
        while (i < 2 * Math.PI) {
            i += rad;
            list.add(new float[]{radius * Mth.cos(i), radius * Mth.sin(i)});
        }
        return list;
    }

    /**
     * Gets a list of vectors rotated around the given axis by the given angles
     *
     * @param dir    The vector to rotate
     * @param axis   The axis to rotate the vector around
     * @param minDeg Minimum rotation in degrees
     * @param maxDeg Maximum rotation in degrees
     * @param step   Angle change per rotation in degrees
     */
    public static List<Vector3f> rotatedVecs(Vector3f dir, Vector3f axis, float minDeg, float maxDeg, float step) {
        List<Vector3f> list = new ArrayList<>();
        list.add(new Vector3f(dir));
        for (float y = step; y <= maxDeg; y += step) {
            list.add(dir.rotateAxis(y * Mth.DEG_TO_RAD, axis.x(), axis.y(), axis.z(), new Vector3f()));
        }
        for (float y = minDeg; y <= -step; y += step) {
            list.add(dir.rotateAxis(y * Mth.DEG_TO_RAD, axis.x(), axis.y(), axis.z(), new Vector3f()));
        }
        return list;
    }

    public static List<Vector3d> rotatedVecs(Vector3d dir, Vector3d axis, float minDeg, float maxDeg, float step) {
        List<Vector3d> list = new ArrayList<>();
        list.add(new Vector3d(dir));
        for (float y = step; y <= maxDeg; y += step) {
            list.add(dir.rotateAxis(y * Mth.DEG_TO_RAD, axis.x(), axis.y(), axis.z(), new Vector3d()));
        }
        for (float y = minDeg; y <= -step; y += step) {
            list.add(dir.rotateAxis(y * Mth.DEG_TO_RAD, axis.x(), axis.y(), axis.z(), new Vector3d()));
        }
        return list;
    }

    public static Vec3 closestPointToLine(Vec3 point, Vec3 from, Vec3 dir) {
        if (dir.equals(Vec3.ZERO))
            return from;
        double lengthSq = dir.lengthSqr();
        double x = Math.max(0, Math.min(1, point.subtract(from).dot(dir) / lengthSq));
        return from.add(dir.scale(x));
    }

    public static double[] rotate2d(double x, double y, double angle) {
        return new double[]{x * Math.cos(angle) - y * Math.sin(angle), y * Math.cos(angle) + x * Math.sin(angle)};
    }

    /**
     * Creates an array of all corner points of a regular polygon
     *
     * @param shape The number of corners of the polygon. E.g. 3 for a triangle
     * @param width The width of the outer circle
     * @return Array of pairs of (x, y) coords
     */
    public static double[][] createRegularPolygonPoints(int shape, float width) {
        if (shape <= 2)
            throw new IllegalArgumentException("Can't create a polygon with 2 or less corners!");
        double[][] res = new double[shape][];
        Vector3d base = new Vector3d(width, 0, 0);
        float rotatePer = 360f / shape;
        if (shape % 2 == 0)
            base = base.rotateAxis(rotatePer * 0.5f * Mth.DEG_TO_RAD, 0, 0, 1);
        res[0] = new double[]{base.x, base.y};
        for (int i = 1; i < shape; i++) {
            Vector3d rotated = base.rotateAxis((rotatePer * i) * Mth.DEG_TO_RAD, 0, 0, 1, new Vector3d());
            res[i] = new double[]{rotated.x, rotated.y};
        }
        return res;
    }

    /**
     * Creates an array of all corner points of a regular polygon
     *
     * @param shape The number of corners of the polygon. E.g. 3 for a triangle
     * @param width The width of the outer circle
     * @return Array of pairs of (x, y) coords
     */
    public static float[][] createRegularPolygonPointsF(int shape, float width) {
        if (shape <= 2)
            throw new IllegalArgumentException("Can't create a polygon with 2 or less corners!");
        float[][] res = new float[shape][];
        Vector3f base = new Vector3f(width, 0, 0);
        float rotatePer = 360f / shape;
        if (shape % 2 == 0)
            base = base.rotateAxis(rotatePer * 0.5f * Mth.DEG_TO_RAD, 0, 0, 1);
        res[0] = new float[]{base.x, base.y};
        for (int i = 1; i < shape; i++) {
            Vector3f rotated = base.rotateAxis((rotatePer * i) * Mth.DEG_TO_RAD, 0, 0, 1, new Vector3f());
            res[i] = new float[]{rotated.x, rotated.y};
        }
        return res;
    }

    /**
     * Returns the points on the given AABBs that are the closest to each other.
     */
    public static Pair<Vec3, Vec3> closestPointsAABB(AABB axisalignedbb, AABB axisalignedbb2) {
        Vec3 first = new Vec3(axisalignedbb.minX <= axisalignedbb2.minX ? axisalignedbb2.minX : axisalignedbb.maxX >= axisalignedbb2.maxX ? axisalignedbb2.maxX : axisalignedbb.minX,
                axisalignedbb.minY <= axisalignedbb2.minY ? axisalignedbb2.minY : axisalignedbb.maxY >= axisalignedbb2.maxY ? axisalignedbb2.maxY : axisalignedbb.minY,
                axisalignedbb.minZ <= axisalignedbb2.minZ ? axisalignedbb2.minZ : axisalignedbb.maxZ >= axisalignedbb2.maxZ ? axisalignedbb2.maxZ : axisalignedbb.minZ);
        Vec3 second = new Vec3(axisalignedbb2.minX <= axisalignedbb.minX ? axisalignedbb.minX : axisalignedbb2.maxX >= axisalignedbb.maxX ? axisalignedbb.maxX : axisalignedbb2.minX,
                axisalignedbb2.minY <= axisalignedbb.minY ? axisalignedbb.minY : axisalignedbb2.maxY >= axisalignedbb.maxY ? axisalignedbb.maxY : axisalignedbb2.minY,
                axisalignedbb2.minZ <= axisalignedbb.minZ ? axisalignedbb.minZ : axisalignedbb2.maxZ >= axisalignedbb.maxZ ? axisalignedbb.maxZ : axisalignedbb2.minZ);
        return Pair.of(first, second);
    }

    /**
     * Rough distance to the given entity from the given ray. Kinda bruteforcing it since everything else i tried didnt have
     * the desired effect
     */
    public static double distTo(Entity e, Vec3 from, Vec3 to) {
        double d = Double.MAX_VALUE;
        Vec3 dir = to.subtract(from);
        for (double height = 0; height <= e.getBbHeight(); height += e.getBbHeight() * 0.1) {
            Vec3 point = e.position().add(0, height, 0);
            double nD = MathUtils.closestPointToLine(point, from, dir).distanceToSqr(point);
            if (nD < d) {
                d = nD;
            }
        }
        return d;
    }

    public static Vec3 farestPointToLine(Vec3 point, Vec3 l1, Vec3 dir) {
        return new Vec3(Math.abs(l1.x - point.x) > Math.abs(dir.x - point.x) ? l1.x : dir.x,
                Math.abs(l1.y - point.y) > Math.abs(dir.y - point.y) ? l1.y : dir.y,
                Math.abs(l1.z - point.z) > Math.abs(dir.z - point.z) ? l1.z : dir.z);
    }

    public static double roundTo(double val, double step) {
        return Math.round(val / (float) step) * step;
    }
}
