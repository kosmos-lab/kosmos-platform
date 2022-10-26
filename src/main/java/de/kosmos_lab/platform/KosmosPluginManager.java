package de.kosmos_lab.platform;

import de.kosmos_lab.platform.web.IAuthProvider;
import org.apache.commons.lang3.ClassUtils;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * The KosmoS Plugin manager is used to dynamically load and start plugins and is based on pf4j
 */
public class KosmosPluginManager extends DefaultPluginManager {
    protected static final Logger logger = LoggerFactory.getLogger("KosmosPluginManager");

    private final HashSet<Class> classes = new HashSet<>();

    /**
     * get a new instance of the plugin manager, automatically inits the system - ie looks for plugins and loads them
     */
    public KosmosPluginManager() {
        super();
        init();
    }

    /**
     * find a class in the current loaded environment
     *
     * @param name
     *
     * @return
     *
     * @throws ClassNotFoundException
     */
    public @Nonnull
    Class
    getClass(@Nonnull String name) throws ClassNotFoundException {
        for (Class extension : classes) {
            if (extension.getCanonicalName().equals(name)) {
                return extension;
            }
            if (extension.getName().equals(name)) {
                return extension;
            }
        }
        throw new ClassNotFoundException(String.format("Could not find class %s", name));
    }

    /**
     * get all the classes that implement this or are super classes
     *
     * @param className
     *
     * @return
     */
    public @Nonnull
    Collection<Class> getClassesFor(@Nonnull Class className) {
        Collection<Class> matched = new HashSet<>();
        for (Class extension : classes) {

            for (Class i : ClassUtils.getAllSuperclasses(extension)) {
                if (className.isAssignableFrom(i)) {
                    matched.add(extension);
                }
            }
        }
        return matched;
    }
    public @Nonnull
    Collection<Class> getAllClassesFor(@Nonnull Class className) {
        Collection<Class> matched = new HashSet<>();
        for (Class extension : classes) {

            for (Class i : ClassUtils.getAllSuperclasses(extension)) {
                if (className.isAssignableFrom(i)) {
                    matched.add(extension);
                }
            }
        }
        Reflections r = new Reflections("de.kosmos_lab");
        for (Class<? extends IAuthProvider> c : r.getSubTypesOf(IAuthProvider.class)) {
            matched.add(c);
        }



            return matched;
    }
    /**
     * get a new instance of the given class
     *
     * @param className
     * @param params
     * @param <T>
     *
     * @return
     *
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public <T> T getInstance(@Nonnull String className, @CheckForNull Object params) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<? extends T> c = getClass(className);
        if (params == null) {
            return c.getConstructor().newInstance();
        }
        return c.getConstructor(params.getClass()).newInstance(params);


    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * initialize the plugin manager
     */
    public void init() {
        for (Path p : this.pluginsRoots) {
            logger.info("found Plugin Path {}", p);
            if (p != null) {
                try {
                    if (p.toFile().exists()) {
                        for (File f : p.toFile().listFiles()) {
                            if (f.isDirectory()) {
                                logger.info("cleaning old data for {}", f);
                                deleteDirectory(f);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Exception:", ex);
                }
            }
        }
        loadPlugins();
        startPlugins();
        List<PluginWrapper> startedPlugins = getStartedPlugins();
        for (PluginWrapper plugin : startedPlugins) {
            try {
                String pluginId = plugin.getDescriptor().getPluginId();

                List<Class<?>> extensionClasses = getExtensionClasses(pluginId);
                if (!extensionClasses.isEmpty()) {
                    for (Class extension : extensionClasses) {
                        logger.info("Extension {} added by {}", extension, plugin.getPluginId());
                        this.classes.add(extension);

                    }
                    try {
                        URL r = extensionClasses.get(0).getResource("/web");
                        if (r != null) {
                            URI uri = r.toURI();
                            Path myPath = null;
                            if (uri.getScheme().equals("jar")) {
                                try {
                                    FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                                    myPath = fileSystem.getPath("/web/");
                                } catch (FileSystemAlreadyExistsException ex) {
                                    logger.error("could not create filesystem for {}", uri);

                                }
                            } else {
                                myPath = Paths.get(uri);
                            }
                            if (myPath != null) {
                                Stream<Path> walk = Files.walk(myPath);

                                for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                                    Path path = it.next();
                                    String filteredName = path.toString().substring(myPath.toString().length());

                                    if (filteredName.length() > 0) {


                                        try {
                                            logger.info("Resource Scanner: {}- {}", path, filteredName);
                                            if (path.toFile().isFile()) {
                                                Path to = new File(String.format("./web/%s", filteredName)).toPath();
                                                logger.info("Found resource to copy in {}: {} ", plugin.getPluginId(), filteredName);
                                                Files.copy(path, to, StandardCopyOption.REPLACE_EXISTING);
                                            } else if (path.toFile().isDirectory()) {
                                                logger.info("Found resource to create in {}: {}", plugin.getPluginId(), filteredName);
                                                new File(String.format("./web/%s", filteredName)).mkdirs();
                                            }
                                        } catch (Exception ex) {
                                           // logger.error("Could not parse Path {} - {}", path, filteredName, ex);
                                            //ex.printStackTrace();
                                        }
                                        //logger.warn("Path File is null?! {}", path);


                                    }
                                }
                            }
                        }
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception ex) {
                logger.error("Exception:", ex);
            }
        }
    }

}

