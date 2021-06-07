package com.flemmli97.tenshilib.common.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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
        float d0 = MathHelper.sqrt(x * x + y * y);
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
            list.add(new float[]{radius * MathHelper.cos(i), radius * MathHelper.sin(i)});
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
    public static Vector3d rotate(Vector3d rotAxis, Vector3d vec, float angle) {
        double[] res = rotate(rotAxis.x, rotAxis.y, rotAxis.z, vec.x, vec.y, vec.z, angle);
        return new Vector3d(res[0], res[1], res[2]);
    }

    /**
     * Rotates a vector around a rotation axis with the given angle
     */
    public static double[] rotate(double axisX, double axisY, double axisZ, double vecX, double vecY, double vecZ, float angle) {
        double rot = axisX * vecX + axisY * vecY + axisZ * vecZ;
        double x = axisX * rot * (1 - MathHelper.cos(angle)) + vecX * MathHelper.cos(angle) + (-axisZ * vecY + axisY * vecZ) * MathHelper.sin(angle);
        double y = axisY * rot * (1 - MathHelper.cos(angle)) + vecY * MathHelper.cos(angle) + (axisZ * vecX - axisX * vecZ) * MathHelper.sin(angle);
        double z = axisZ * rot * (1 - MathHelper.cos(angle)) + vecZ * MathHelper.cos(angle) + (-axisY * vecX + axisX * vecY) * MathHelper.sin(angle);
        return new double[] {x, y, z};
    }

    public static Vector3d closestPointToLine(Vector3d point, Vector3d from, Vector3d dir) {
        if (dir.equals(Vector3d.ZERO))
            return from;
        double lengthSq = dir.lengthSquared();
        double x = Math.max(0, Math.min(1, point.subtract(from).dotProduct(dir) / lengthSq));
        return from.add(dir.scale(x));
    }

    /**
     * Checks if the given point is in front of the line.
     * The line here is assumed to be a finite line with the given starting point.
     *
     * @param pos  The given point
     * @param from The starting point of the line
     * @param dir  The direction vector of the line.
     */
    public static boolean isInFront(Vector3d pos, Vector3d from, Vector3d dir) {
        return from.add(dir).squareDistanceTo(pos) < from.subtract(dir).squareDistanceTo(pos);
    }

    /**
     * Returns the points on the given AABBs that are the closest to each other.
     */
    public static Pair<Vector3d, Vector3d> closestPointsAABB(AxisAlignedBB axisalignedbb, AxisAlignedBB axisalignedbb2) {
        Vector3d first = new Vector3d(axisalignedbb.minX <= axisalignedbb2.minX ? axisalignedbb2.minX : axisalignedbb.maxX >= axisalignedbb2.maxX ? axisalignedbb2.maxX : axisalignedbb.minX,
                axisalignedbb.minY <= axisalignedbb2.minY ? axisalignedbb2.minY : axisalignedbb.maxY >= axisalignedbb2.maxY ? axisalignedbb2.maxY : axisalignedbb.minY,
                axisalignedbb.minZ <= axisalignedbb2.minZ ? axisalignedbb2.minZ : axisalignedbb.maxZ >= axisalignedbb2.maxZ ? axisalignedbb2.maxZ : axisalignedbb.minZ);
        Vector3d second = new Vector3d(axisalignedbb2.minX <= axisalignedbb.minX ? axisalignedbb.minX : axisalignedbb2.maxX >= axisalignedbb.maxX ? axisalignedbb.maxX : axisalignedbb2.minX,
                axisalignedbb2.minY <= axisalignedbb.minY ? axisalignedbb.minY : axisalignedbb2.maxY >= axisalignedbb.maxY ? axisalignedbb.maxY : axisalignedbb2.minY,
                axisalignedbb2.minZ <= axisalignedbb.minZ ? axisalignedbb.minZ : axisalignedbb2.maxZ >= axisalignedbb.maxZ ? axisalignedbb.maxZ : axisalignedbb2.minZ);
        return Pair.of(first, second);
    }

    /**
     * Rough distance to the given entity from the given ray. Kinda bruteforcing it since everything else i tried didnt have
     * the desired effect
     */
    public static double distTo(Entity e, Vector3d from, Vector3d to) {
        double d = Double.MAX_VALUE;
        Vector3d dir = to.subtract(from);
        for (double height = 0; height <= e.getHeight(); height += e.getHeight() * 0.1) {
            Vector3d point = e.getPositionVec().add(0, height, 0);
            double nD = MathUtils.closestPointToLine(point, from, dir).squareDistanceTo(point);
            if (nD < d) {
                d = nD;
            }
        }
        return d;
    }

    public static Vector3d farestPointToLine(Vector3d point, Vector3d l1, Vector3d dir) {
        return new Vector3d(Math.abs(l1.x - point.x) > Math.abs(dir.x - point.x) ? l1.x : dir.x,
                Math.abs(l1.y - point.y) > Math.abs(dir.y - point.y) ? l1.y : dir.y,
                Math.abs(l1.z - point.z) > Math.abs(dir.z - point.z) ? l1.z : dir.z);
    }

    public static double roundTo(double val, double step) {
        return Math.round(val / (float) step) * step;
    }
}
