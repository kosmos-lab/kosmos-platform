package de.kosmos_lab.platform.gesture.data;

import de.kosmos_lab.platform.gesture.utils.Geometry;
import de.kosmos_lab.utils.StringFunctions;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;

/**
 * Implements a gesture as a cloud of points (i.e., an unordered set of points).
 * For $P, gestures are normalized with respect to scale, translated to origin, and resampled into a fixed number of 32 points.
 * For $Q, a LUT is also computed.
 */
public class Gesture {
    private static final int SAMPLING_RESOLUTION = 32;                             // default number of points on the gesture path
    private static final int MAX_INT_COORDINATES = 1024;                           // $Q only: each point has two additional x and y integer coordinates in the interval [0..MAX_INT_COORDINATES-1] used to operate the LUT table efficiently (O(1))
    public final static int LUT_SIZE = 64;                                        // $Q only: the default size of the lookup table is 64 x 64
    public final static int LUT_SCALE_FACTOR = MAX_INT_COORDINATES / LUT_SIZE;    // $Q only: scale factor to convert between integer x and y coordinates and the size of the LUT
    public final Point[] rawPoints;
    public final String Id;
    public Point[] points = null;            // gesture points (normalized)
    public String name;                 // gesture class
    public int[][] lookupTable = null;               // lookup table
    
    /**
     * Constructs a gesture from an array of points
     *
     * @param points
     * @param gestureName
     */
    public Gesture(@Nonnull Point[] points, @Nonnull String gestureName, @Nonnull String id) {
        this(points, gestureName, true, id);
    }
    
    /**
     * Constructs a gesture from an array of points
     *
     * @param points
     */
    public Gesture(@Nonnull Point[] points) {
        
        this(points, "", true, StringFunctions.generateRandomKey(26));
        
        
    }
    
    
    /**
     * Constructs a gesture from an array of points
     *
     * @param points
     * @param gestureName
     * @param constructLUT
     */
    public Gesture(@Nonnull Point[] points, @Nonnull String gestureName, boolean constructLUT, @Nonnull String id) {
        this.Id = id;
        this.name = gestureName;
        this.rawPoints = points;
        // normalizes the array of points with respect to scale, origin, and number of points
        this.points = Scale(points);
        this.points = TranslateTo(this.points, Centroid(this.points));
        this.points = Resample(this.points, SAMPLING_RESOLUTION);
        
        if (constructLUT) {
            // constructs a lookup table for fast lower bounding (used by $Q)
            this.TransformCoordinatesToIntegers();
            this.ConstructLUT();
        }
    }
    

    
    //region gesture pre-processing steps: scale normalization, translation to origin, and resampling
    
    /**
     * Computes the centroid for an array of points
     *
     * @param points
     * @return
     */
    private Point Centroid(@Nonnull Point[] points) {
        float cx = 0, cy = 0;
        for (int i = 0; i < points.length; i++) {
            cx += points[i].x;
            cy += points[i].y;
        }
        return new Point(cx / points.length, cy / points.length, 0);
    }
    
    /**
     * Constructs a Lookup Table that maps grip points to the closest point from the gesture path
     */
    private void ConstructLUT() {
        this.lookupTable = new int[LUT_SIZE][];
        for (int i = 0; i < LUT_SIZE; i++)
            lookupTable[i] = new int[LUT_SIZE];
        
        for (int i = 0; i < LUT_SIZE; i++)
            for (int j = 0; j < LUT_SIZE; j++) {
                int minDistance = Integer.MAX_VALUE;
                int indexMin = -1;
                for (int t = 0; t < points.length; t++) {
                    int row = points[t].lookupY / LUT_SCALE_FACTOR;
                    int col = points[t].lookupX / LUT_SCALE_FACTOR;
                    int dist = (row - i) * (row - i) + (col - j) * (col - j);
                    if (dist < minDistance) {
                        minDistance = dist;
                        indexMin = t;
                    }
                }
                lookupTable[i][j] = indexMin;
            }
    }
    
    /**
     * Computes the path length for an array of points
     *
     * @param points
     * @return
     */
    private float PathLength(@Nonnull Point[] points) {
        float length = 0;
        for (int i = 1; i < points.length; i++)
            if (points[i].stroke == points[i - 1].stroke)
                length += Geometry.euclideanDistance(points[i - 1], points[i]);
        return length;
    }
    
    /**
     * Resamples the array of points into n equally-distanced points
     *
     * @param points
     * @param n
     * @return
     */
    @Nonnull public Point[] Resample(@Nonnull Point[] points, int n) {
        Point[] newPoints = new Point[n];
        newPoints[0] = new Point(points[0].x, points[0].y, points[0].stroke);
        int numPoints = 1;
        
        float I = PathLength(points) / (n - 1); // computes interval length
        float D = 0;
        for (int i = 1; i < points.length; i++) {
            if (points[i].stroke == points[i - 1].stroke) {
                float d = Geometry.euclideanDistance(points[i - 1], points[i]);
                if (D + d >= I) {
                    Point firstPoint = points[i - 1];
                    while (D + d >= I) {
                        // add interpolated point
                        float t = Math.min(Math.max((I - D) / d, 0.0f), 1.0f);
                        if (Float.isNaN(t)) t = 0.5f;
                        newPoints[numPoints++] = new Point(
                                (1.0f - t) * firstPoint.x + t * points[i].x,
                                (1.0f - t) * firstPoint.y + t * points[i].y,
                                points[i].stroke
                        );
                        
                        // update partial length
                        d = D + d - I;
                        D = 0;
                        firstPoint = newPoints[numPoints - 1];
                    }
                    D = d;
                } else D += d;
            }
        }
        
        if (numPoints == n - 1) // sometimes we fall a rounding-error short of adding the last point, so add it if so
            newPoints[numPoints++] = new Point(points[points.length - 1].x, points[points.length - 1].y, points[points.length - 1].stroke);
        return newPoints;
    }
    
    /**
     * Performs scale normalization with shape preservation into [0..1]x[0..1]
     *
     * @param points
     * @return
     */
    @Nonnull private Point[] Scale(@Nonnull Point[] points) {
        float minx = Float.MAX_VALUE, miny = Float.MAX_VALUE, maxx = -Float.MAX_VALUE, maxy = -Float.MAX_VALUE;
        for (int i = 0; i < points.length; i++) {
            if (minx > points[i].x) minx = points[i].x;
            if (miny > points[i].y) miny = points[i].y;
            if (maxx < points[i].x) maxx = points[i].x;
            if (maxy < points[i].y) maxy = points[i].y;
        }
        
        Point[] newPoints = new Point[points.length];
        float scale = Math.max(maxx - minx, maxy - miny);
        for (int i = 0; i < points.length; i++)
            newPoints[i] = new Point((points[i].x - minx) / scale, (points[i].y - miny) / scale, points[i].stroke);
        return newPoints;
    }
    
    /**
     * Scales point coordinates to the integer domain [0..MAXINT-1] x [0..MAXINT-1]
     */
    private void TransformCoordinatesToIntegers() {
        for (int i = 0; i < points.length; i++) {
            points[i].lookupX = (int) ((points[i].x + 1.0f) / 2.0f * (MAX_INT_COORDINATES - 1));
            points[i].lookupY = (int) ((points[i].y + 1.0f) / 2.0f * (MAX_INT_COORDINATES - 1));
        }
    }
    
    /**
     * Translates the array of points by p
     *
     * @param points
     * @param p
     * @return
     */
    @Nonnull private Point[] TranslateTo(@Nonnull Point[] points,@Nonnull  Point p) {
        Point[] newPoints = new Point[points.length];
        for (int i = 0; i < points.length; i++)
            newPoints[i] = new Point(points[i].x - p.x, points[i].y - p.y, points[i].stroke);
        return newPoints;
    }

    @Nonnull public JSONArray pointsToJSON() {
        JSONArray points = new JSONArray();
        for (Point p : rawPoints) {
            points.put(new JSONArray().put(p.x).put(p.y));
        }
        return points;
    }



    @Nonnull public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        
        json.put("name", this.name);
        
        json.put("points", pointsToJSON());
        return json;
    }
    
    //endregion
}