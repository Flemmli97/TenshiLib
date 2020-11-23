package com.flemmli97.tenshilib.common.utils;

import com.google.common.collect.Lists;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

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
        List<float[]> list = Lists.newArrayList();
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
        double rot = rotAxis.x * vec.x + rotAxis.y * vec.y + rotAxis.z * vec.z;
        double x = rotAxis.x * rot * (1 - MathHelper.cos(angle))
                + vec.x * MathHelper.cos(angle) + (-rotAxis.z * vec.y + rotAxis.y * vec.z) * MathHelper.sin(angle);

        double y = rotAxis.y * rot * (1 - MathHelper.cos(angle))
                + vec.y * MathHelper.cos(angle) + (rotAxis.z * vec.x - rotAxis.x * vec.z) * MathHelper.sin(angle);

        double z = rotAxis.z * rot * (1 - MathHelper.cos(angle))
                + vec.z * MathHelper.cos(angle) + (-rotAxis.y * vec.x + rotAxis.x * vec.y) * MathHelper.sin(angle);
        return new Vector3d(x, y, z);
    }

    public static Vector3d closestPointToLine(Vector3d point, Vector3d from, Vector3d dir) {
        if(dir.equals(Vector3d.ZERO))
            return from;
        double lengthSq = dir.lengthSquared();
        double x = Math.max(0, Math.min(1, point.subtract(from).dotProduct(dir)/lengthSq));
        return from.add(dir.scale(x));
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
