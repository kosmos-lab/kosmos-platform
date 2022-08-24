package de.kosmos_lab.platform.gesture;


import de.kosmos_lab.platform.gesture.data.Gesture;
import de.kosmos_lab.platform.gesture.data.Point;
import de.kosmos_lab.utils.FileUtils;
import de.kosmos_lab.utils.StringFunctions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.JSONArray;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * the GestureProvider reads the gestures from the system and provides those to the system
 */
@SuppressFBWarnings("DM_EXIT")
public class GestureProvider {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("GestureProvider");
    public final static Pattern jsonMatcher = Pattern.compile("^(?<name>.*)_(?<key>[A-Za-z0-9]{26})\\.json$");
    private final String path;
    private final File dir;
    private final ConcurrentLinkedQueue<Gesture> gestures = new ConcurrentLinkedQueue<>();


    /**
     * create a new instance of the GestureProvider
     *
     * @param readGestures
     * @param path
     */
    public GestureProvider(boolean readGestures, @Nonnull String path) {
        this.path = path;
        dir = new File(path);
        if (!dir.exists()) {

            if (!dir.mkdirs()) {
                logger.warn("could not create gesture folder \"{}\" - exiting", dir);
                System.exit(1);
            }
        }
        if (readGestures) {
            for (GestureFile gf : getGestureFiles()) {
                try {
                    JSONArray points = new JSONArray(FileUtils.readFile(gf.file));
                    logger.info("Read gesture {}({}) with {} Points", gf.name, gf.key, points.length());
                    Gesture g = new Gesture(readPoints(points).toArray(Point[]::new), gf.name, gf.key);

                    this.gestures.add(g);
                    //this.addGesture(gf.name, new JSONArray(FileUtils.readFile(gf.file)), false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            logger.info("Found {} gestures", gestures.size());
        }
    }


    public boolean addGesture(@Nonnull String name, @Nonnull JSONArray arr, boolean save, @Nonnull String key) {
        //first try saving
        //maybe the gesture already is known, but not saved
        if (save) {
            saveGesture(name, arr, key);

        }


        Gesture g = new Gesture(readPoints(arr).toArray(Point[]::new), name, key);
        // generate the string for the points
        String newGesturePoints = g.pointsToJSON().toString();
        // check if this exact gesture is already known
        for (Gesture g2 : this.gestures) {
            String existingGesturePoints = g2.pointsToJSON().toString();
            if (newGesturePoints.equals(existingGesturePoints)) {
                return false;
            }
        }
        this.gestures.add(g);
        return true;
    }

    public boolean addGesture(@Nonnull String name, @Nonnull JSONArray arr, boolean save) {
        if (dir == null) {
            throw new IllegalArgumentException("Gesture directory not set");
        }
        boolean used = false;
        String key = null;
        //generate a random key, generate a new one until we found an unused one.
        do {
            key = StringFunctions.generateRandomKey(26);

            //potentially get a better speed if the listFiles would not be done inside the loop - but its cleaner and the risk of a second iteration is very slim
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(key + ".json")) {
                        used = true;
                        break;
                    }
                }
            }
        } while (used);
        return addGesture(name, arr, save, key);
    }

    public void addGesture(@Nonnull Gesture g) {
        gestures.add(g);
    }

    public boolean deleteGesture(@Nonnull String id) {
        boolean found = false;
        for (Iterator<Gesture> it = this.gestures.iterator(); it.hasNext(); ) {
            Gesture g = it.next();
            if (g.Id.equals(id)) {
                gestures.remove(g);
                found = true;
                break;
            }
        }

        for (GestureFile gf : getGestureFiles()) {
            if (gf.key.equals(id)) {
                if (gf.file.exists()) {
                    if (!gf.file.delete()) {

                        logger.warn("could not delete gesture file \"{}\"", gf.file);

                    }
                }

                found = true;
                break;
            }
        }
        return found;
    }

    @Nonnull
    public Collection<GestureFile> getGestureFiles(@Nonnull String gesture) {
        Collection<GestureFile> list = new LinkedList<>();
        for (GestureFile gf : getGestureFiles()) {
            if (gf.name.equals(gesture)) {
                list.add(gf);
            }
        }

        return list;
    }

    @Nonnull
    public Collection<GestureFile> getGestureFiles() {
        Collection<GestureFile> list = new LinkedList<>();

        if (dir == null) {
            throw new IllegalArgumentException("Gesture directory not set");
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {

                Matcher m = jsonMatcher.matcher(f.getName());
                if (m.matches()) {
                    list.add(new GestureFile(m.group("name"), f, m.group("key")));
                }

            }
        }
        return list;
    }

    @Nonnull
    public Collection<Gesture> listGestures() {

        return this.gestures;
    }

    @CheckForNull
    public Gesture predict(JSONArray arr) {
        return predict(new Gesture(readPoints(arr).toArray(Point[]::new)));

    }

    @CheckForNull
    public Gesture predict(@Nonnull Gesture gesture) {
        return QPointCloudRecognizer.Classify(gesture, this.gestures);


    }

    @Nonnull
    public LinkedList<Point> readPoints(@Nonnull JSONArray arr) {
        LinkedList<Point> pts = new LinkedList<Point>();
        for (int i = 0; i < arr.length(); i++) {
            JSONArray ptarr = arr.getJSONArray(i);

            for (int j = 0; j < ptarr.length(); j++) {
                try {
                    Object o = ptarr.get(j);
                    if (o instanceof JSONArray) {
                        JSONArray a = ptarr.getJSONArray(j);
                        //logger.info("reading: {}", a);
                        pts.add(new Point(Double.valueOf(a.getDouble(0)).floatValue(), Double.valueOf(a.getDouble(1)).floatValue(), i));
                    } else {
                        logger.warn("type of o is {} {}", o.getClass(), o);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }
        }

        return pts;
    }

    @Nonnull
    public boolean renameGesture(@Nonnull String from, @Nonnull String to) {
        boolean found = false;
        for (Gesture g : this.gestures) {
            if (g.name.equals(from)) {
                File f = getGestureFile(g);
                g.name = to;
                File f2 = getGestureFile(g);
                if (!f.renameTo(f2)) {
                    return false;
                }

                //this.saveGesture(g);
                found = true;
            }

        }
        return found;
    }

    @Nonnull
    public File getGestureFile(@Nonnull String name, @Nonnull String key) {
        return new File(path + "/" + name + "_" + key + ".json");
    }

    @Nonnull
    public File getGestureFile(@Nonnull Gesture g) {
        return getGestureFile(g.name, g.Id);
    }

    public boolean saveGesture(@Nonnull String name, @Nonnull JSONArray arr, @Nonnull String key) {
        String str = arr.toString();
        //check ALL gesture files
        boolean save = false;
        for (GestureFile gf : getGestureFiles()) {
            try {
                if (FileUtils.readFile(gf.file).equals(str)) {
                    save = false;
                    logger.debug("skipping to save gesture, its already there");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileUtils.writeToFile(getGestureFile(name, key), str);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void saveGesture(@Nonnull Gesture g) {
        saveGesture(g.name, g.pointsToJSON(), g.Id);
    }

    private static class GestureFile {
        protected final String name;
        protected final File file;
        protected final String key;

        private GestureFile(@Nonnull String name, @Nonnull File file, @Nonnull String key) {
            this.file = file;
            this.name = name;
            this.key = key;


        }
    }
}
