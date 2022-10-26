package de.kosmos_lab.platform.client;

import de.kosmos_lab.platform.web.servlets.KosmoSServlet;
import de.kosmos_lab.web.doc.openapi.ApiEndpoint;

import java.util.concurrent.ConcurrentHashMap;

public class KosmoSPathHelper {
    static final ConcurrentHashMap<Class, String> pathCache = new ConcurrentHashMap<>();

    public static String getPath(Class<? extends KosmoSServlet> clazz) {
        String path = pathCache.get(clazz);
        if (path == null) {
            ApiEndpoint annotation = clazz.getAnnotation(ApiEndpoint.class);
            if (annotation != null) {
                path = annotation.path();
                pathCache.put(clazz, path);
            } else {
                throw new RuntimeException("no path found!");
            }
        }


        return path;
    }
}
