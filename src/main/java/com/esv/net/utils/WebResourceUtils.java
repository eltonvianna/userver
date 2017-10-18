/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.esv.net.HttpRequest;
import com.esv.utile.logging.Logger;
import com.esv.utile.utils.CharSequenceUtils;
import com.esv.utile.utils.ObjectUtils;
import com.esv.utile.utils.PropertiesUtils;
import com.esv.utile.utils.ResourceUtils;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 29/09/2017
 */
public final class WebResourceUtils {
    
    private static final Logger LOGGER = Logger.getLogger(WebResourceUtils.class);
    
    private static final Map<String, String> webResourcesMap = new ConcurrentHashMap<>();
    private static final String defaultPage;
    
    /**
     * <p>Create a web resource map</p>
     */
    static {
        try {
            final String directoryName = PropertiesUtils.getStringProperty("resources.dir", "htdocs");
            final String webResourcesDir = "/".equals(directoryName) ? "htdocs" : directoryName;
            final String defaultPageName = PropertiesUtils.getStringProperty("default.page", "main.html");
            defaultPage = ResourceUtils.normalize(webResourcesDir + "/" + defaultPageName);
            LOGGER.debug(() -> "Default web page: " + defaultPage);
            WebResourceUtils.scanWebResources(ResourceUtils.normalize(webResourcesDir));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Suppressing default constructor for non instantiability
     */
    private WebResourceUtils() {
        throw new AssertionError("Suppress default constructor for non instantiability");
    }
    
    /**
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void scanWebResources(final String webResourcesDir) throws IOException, URISyntaxException {
        // list all files of web resource directory
        final List<Path> resources = ResourceUtils.list(webResourcesDir);
        LOGGER.trace(() -> "Resources found: " + resources);
        for (final Path resource : resources) {
            final String name = ResourceUtils.normalize(resource.toString());
            final int idx = name.indexOf(webResourcesDir);
            if (idx > -1) {
                // resourceName key must match with request.getPathInfo()
                final String resourceName = name.substring(idx + webResourcesDir.length());
                final String resourcePath = (webResourcesDir + resourceName);
                webResourcesMap.put(resourceName, resourcePath);
                LOGGER.debug(() -> "Added resource name: " + resourceName + ", resource path: " + resourcePath);
            }
        }
        LOGGER.debug(() -> "Resources dir: "+ webResourcesDir + ", default page: " + defaultPage + ", resource set: " + webResourcesMap.keySet());
    }
    
    /**
     * @param httpRequest 
     * @param maxPathLevel
     * @param defaultResourceName
     * @return
     */
    public static String lookup(final HttpRequest httpRequest, final int maxPathLevel, final String defaultResourceName) {
        ObjectUtils.requireNotNull(httpRequest, "request parameter is null");
        CharSequenceUtils.requireNotBlank(defaultResourceName, "defaultResourceName parameter is null");
        return httpRequest.getPathLevel() > maxPathLevel ? webResourcesMap.get(httpRequest.getPathInfo()) : webResourcesMap.getOrDefault(httpRequest.getPathInfo(), defaultResourceName);
    }


    /**
     * @return the resourcemap
     */
    public static Map<String, String> resourceMap() {
        return Collections.unmodifiableMap(webResourcesMap);
    }


    /**
     * @return the defaultPage
     */
    public static String defaultPage() {
        return defaultPage;
    }
}