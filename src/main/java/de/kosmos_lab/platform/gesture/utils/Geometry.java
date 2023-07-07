package de.kosmos_lab.platform.gesture.utils;

import de.kosmos_lab.platform.gesture.data.Point;

import javax.annotation.Nonnull;

public class Geometry {
    /**
     * Computes the Squared Euclidean Distance between two points in 2D
     */
    public static float sqrEuclideanDistance(@Nonnull Point a, @Nonnull Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    /**
     * Computes the Euclidean Distance between two points in 2D
     */
    public static float euclideanDistance(@Nonnull Point a, @Nonnull Point b) {
        return (float) Math.sqrt(sqrEuclideanDistance(a, b));
    }
}