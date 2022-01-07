package io.github.flemmli97.tenshilib.common.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {

    public static float degToRad(float degree) {
        return degree * ((float) Math.PI / 180F);
    }

    public static float radToDeg(float rad) {
        return rad * (180 / (float) Math.PI);
    }

    /**
     * Gets closest point on circle circumfence from point
     */
    public static double[] closestOnCircle(double centerX, double centerY, double pointX, double pointY, double radius) {
        double x = pointX - centerX;
        double y = pointY - centerY;
        double d0 = Math.sqrt(x * x + y * y);
        return new double[]{x / d0 * radius + centerX, y / d0 * radius + centerY};
    }

    public static double phiFromPoint(double centerX, double centerY, double pointX, double pointY) {
        return Math.atan2(pointY - centerY, pointX - centerX);
    }

    public static List<float[]> pointsOfCircle(float radius, int density) {
        float rad = degToRad(density);
        float i = -rad;
        List<float[]> list = new ArrayList<>();
        while (i < 2 * Math.PI) {
            i += rad;
            list.add(new float[]{radius * Mth.cos(i), radius * Mth.sin(i)});
        }
        return list;
    }

    /**
     * Rotates a vector around a rotation axis with the given angle
     *
     * @param rotAxis Rotation axis vector. needs to be normalized.
     * @param vec     The vector to rotate
     * @param angle   Angle in radians
     * @return The rotated vector
     */
    public static Vec3 rotate(Vec3 rotAxis, Vec3 vec, float angle) {
        double[] res = rotate(rotAxis.x, rotAxis.y, rotAxis.z, vec.x, vec.y, vec.z, angle);
        return new Vec3(res[0], res[1], res[2]);
    }

    /**
     * Rotates a vector around a rotation axis with the given angle
     */
    public static double[] rotate(double axisX, double axisY, double axisZ, double vecX, double vecY, double vecZ, float angle) {
        double rot = axisX * vecX + axisY * vecY + axisZ * vecZ;
        double x = axisX * rot * (1 - Mth.cos(angle)) + vecX * Mth.cos(angle) + (-axisZ * vecY + axisY * vecZ) * Mth.sin(angle);
        double y = axisY * rot * (1 - Mth.cos(angle)) + vecY * Mth.cos(angle) + (axisZ * vecX - axisX * vecZ) * Mth.sin(angle);
        double z = axisZ * rot * (1 - Mth.cos(angle)) + vecZ * Mth.cos(angle) + (-axisY * vecX + axisX * vecY) * Mth.sin(angle);
        return new double[]{x, y, z};
    }

    public static float[] rotate(float axisX, float axisY, float axisZ, float vecX, float vecY, float vecZ, float angle) {
        float rot = axisX * vecX + axisY * vecY + axisZ * vecZ;
        float x = axisX * rot * (1 - Mth.cos(angle)) + vecX * Mth.cos(angle) + (-axisZ * vecY + axisY * vecZ) * Mth.sin(angle);
        float y = axisY * rot * (1 - Mth.cos(angle)) + vecY * Mth.cos(angle) + (axisZ * vecX - axisX * vecZ) * Mth.sin(angle);
        float z = axisZ * rot * (1 - Mth.cos(angle)) + vecZ * Mth.cos(angle) + (-axisY * vecX + axisX * vecY) * Mth.sin(angle);
        return new float[]{x, y, z};
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
     * Checks if the given point is in front of the line.
     * The line here is assumed to be a finite line with the given starting point.
     *
     * @param pos  The given point
     * @param from The starting point of the line
     * @param dir  The direction vector of the line.
     */
    public static boolean isInFront(Vec3 pos, Vec3 from, Vec3 dir) {
        return from.add(dir).distanceToSqr(pos) < from.subtract(dir).distanceToSqr(pos);
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
