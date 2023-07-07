package de.kosmos_lab.platform.gesture;


import de.kosmos_lab.platform.gesture.data.Gesture;
import de.kosmos_lab.platform.gesture.data.Point;
import de.kosmos_lab.platform.gesture.utils.Geometry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Implements the $Q recognizer
 */
public class QPointCloudRecognizer {
    // $Q's two major optimization layers (Early Abandoning and Lower Bounding)
    // can be activated / deactivated as desired
    public final static boolean useEarlyAbandoning = true;
    public final static boolean useLowerBounding = true;

    /**
     * Main function of the $Q recognizer. Classifies a candidate gesture against a set of templates. Returns the class
     * of the closest neighbor in the template set.
     *
     * @param candidate
     * @param templateSet
     *
     * @return
     */
    @CheckForNull
    static Gesture Classify(@Nonnull Gesture candidate, @Nonnull Collection<Gesture> templateSet) {
        float minDistance = Float.MAX_VALUE;
        Gesture gestureClass = null;
        for (Gesture template : templateSet) {
            float dist = GreedyCloudMatch(candidate, template, minDistance);
            if (dist < minDistance) {
                minDistance = dist;
                gestureClass = template;
            }
        }
        return gestureClass;
    }

    /**
     * Computes the distance between two point clouds by performing a minimum-distance greedy matching starting with
     * point startIndex
     *
     * @param points1
     * @param points2
     * @param startIndex
     *
     * @return
     */
    private static float CloudDistance(@Nonnull Point[] points1, @Nonnull Point[] points2, int startIndex, float minSoFar) {
        int n = points1.length;                // the two point clouds should have the same number of points by now
        int[] indexesNotMatched = new int[n];  // stores point indexes for points from the 2nd cloud that haven't been matched yet
        for (int j = 0; j < n; j++)
            indexesNotMatched[j] = j;

        float sum = 0;                // computes the sum of distances between matched points (i.e., the distance between the two clouds)
        int i = startIndex;           // start matching with point startIndex from the 1st cloud
        int weight = n;               // implements weights, decreasing from n to 1
        int indexNotMatched = 0;      // indexes the indexesNotMatched[..] array of points from the 2nd cloud that are not matched yet
        do {
            int index = -1;
            float minDistance = Float.MAX_VALUE;
            for (int j = indexNotMatched; j < n; j++) {
                float dist = Geometry.sqrEuclideanDistance(points1[i], points2[indexesNotMatched[j]]);  // use the squared Euclidean distance
                if (dist < minDistance) {
                    minDistance = dist;
                    index = j;
                }
            }
            indexesNotMatched[index] = indexesNotMatched[indexNotMatched];  // point indexesNotMatched[index] of the 2nd cloud is now matched to point i of the 1st cloud
            sum += (weight--) * minDistance;           // weight each distance with a confidence coefficient that decreases from n to 1

            if (useEarlyAbandoning) {
                if (sum >= minSoFar)
                    return sum;       // implement early abandoning
            }

            i = (i + 1) % n;                           // advance to the next point in the 1st cloud
            indexNotMatched++;                         // update the number of points from the 2nd cloud that haven't been matched yet
        } while (i != startIndex);
        return sum;
    }

    /**
     * Computes lower bounds for each starting point and the direction of matching from points1 to points2
     */
    private static float[] ComputeLowerBound(@Nonnull Point[] points1, @Nonnull Point[] points2, int[][] LUT, int step) {
        int n = points1.length;
        float[] LB = new float[n / step + 1];
        float[] SAT = new float[n];

        LB[0] = 0;
        for (int i = 0; i < n; i++) {
            int index = LUT[points1[i].lookupY / Gesture.LUT_SCALE_FACTOR][points1[i].lookupX / Gesture.LUT_SCALE_FACTOR];
            float dist = Geometry.sqrEuclideanDistance(points1[i], points2[index]);
            SAT[i] = (i == 0) ? dist : SAT[i - 1] + dist;
            LB[0] += (n - i) * dist;
        }

        for (int i = step, indexLB = 1; i < n; i += step, indexLB++)
            LB[indexLB] = LB[0] + i * SAT[n - 1] - n * SAT[i - 1];
        return LB;
    }

    /**
     * Implements greedy search for a minimum-distance matching between two point clouds. Implements Early Abandoning
     * and Lower Bounding (LUT) optimizations.
     */
    private static float GreedyCloudMatch(@Nonnull Gesture gesture1, @Nonnull Gesture gesture2, float minSoFar) {
        int n = gesture1.points.length;       // the two clouds should have the same number of points by now
        float eps = 0.5f;                     // controls the number of greedy search trials (eps is in [0..1])
        int step = (int) Math.floor(Math.pow(n, 1.0f - eps));

        if (useLowerBounding) {
            float[] LB1 = ComputeLowerBound(gesture1.points, gesture2.points, gesture2.lookupTable, step);  // direction of matching: gesture1 --> gesture2
            float[] LB2 = ComputeLowerBound(gesture2.points, gesture1.points, gesture1.lookupTable, step);  // direction of matching: gesture2 --> gesture1
            for (int i = 0, indexLB = 0; i < n; i += step, indexLB++) {
                if (LB1[indexLB] < minSoFar) {  // direction of matching: gesture1 --> gesture2 starting with index point i
                    minSoFar = Math.min(minSoFar, CloudDistance(gesture1.points, gesture2.points, i, minSoFar));
                }
                if (LB2[indexLB] < minSoFar) {// direction of matching: gesture2 --> gesture1 starting with index point i
                    minSoFar = Math.min(minSoFar, CloudDistance(gesture2.points, gesture1.points, i, minSoFar));
                }
            }
        } else {
            for (int i = 0; i < n; i += step) {
                minSoFar = Math.min(minSoFar, CloudDistance(gesture1.points, gesture2.points, i, minSoFar));  // direction of matching: gesture1 --> gesture2 starting with index point i
                minSoFar = Math.min(minSoFar, CloudDistance(gesture2.points, gesture1.points, i, minSoFar));  // direction of matching: gesture2 --> gesture1 starting with index point i
            }
        }

        return minSoFar;
    }
}