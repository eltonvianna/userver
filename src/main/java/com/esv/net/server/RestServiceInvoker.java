/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.esv.net.HttpRequest;
import com.esv.net.rest.Get;
import com.esv.net.rest.RestService;
import com.esv.utile.logging.Logger;
import com.esv.utile.utils.CharSequenceUtils;
import com.esv.utile.utils.JsonUtils;
import com.esv.utile.utils.ObjectUtils;
import com.esv.utile.utils.PropertiesUtils;
import com.esv.utile.utils.ResourceUtils;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 04/10/2017
 */
final class RestServiceInvoker {
    
    private static final Logger LOGGER = Logger.getLogger(RestServiceInvoker.class);

    private static final Set<String> uriMappings = new HashSet<>();
    private static final Map<String, Method> getMappings = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    
    static {
        try {
            RestServiceInvoker.scanServices();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
     * Suppressing default constructor for non instantiability
     */
    private RestServiceInvoker() {
        throw new AssertionError("Suppress default constructor for non instantiability");
    }
    
    /**
     * @throws Exception
     */
    private static void scanServices() throws Exception {
        final String testOutputDir = PropertiesUtils.getStringProperty("test.output.dir", "test-classes");
        final boolean testContext = ResourceUtils.endsWith(".", testOutputDir);
        final List<String> classes = ResourceUtils.listClasses(true == testContext ? System.getProperty("java.class.path") : ".");
        for (final String className : classes) {
            try {
                final Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(RestService.class)) {
                    LOGGER.debug(() -> "Found RestService annotation at class: " + className);
                    final RestService restService = clazz.getAnnotation(RestService.class);
                    if (true == restService.singleton()) {
                        LOGGER.debug(() -> "Creating a singleton instance of: " + className);
                        if (false == singletons.containsKey(clazz)) {
                            singletons.put(clazz, ObjectUtils.newInstance(clazz));
                        }
                    }
                    scanEndpoints(className, clazz);
                }
            } catch (Exception e) {
                LOGGER.debug(() -> e.getMessage()).trace("Stack trace:", e);
            }
        }
    }
    
    /**
     * @param className
     * @param clazz
     */
    private static void scanEndpoints(final String className, final Class<?> clazz) {
        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Get.class)) {
                addGetEndpoint(method);
            }
        }
    }

    /**
     * @param method
     */
    private static void addGetEndpoint(final Method method) {
        final Get get = method.getAnnotation(Get.class);
        final String currMethodName = ObjectUtils.canonicalMethotName(method);
        LOGGER.debug(() -> "Found Get(\"" + get.value() + "\") annotation at: " + currMethodName);
        final String uri = get.value().trim();
        if (getMappings.containsKey(uri)) {
            final Method m = getMappings.get(uri);
            final String prevMethodName = ObjectUtils.canonicalMethotName(m);
            final String message = "Duplicate Get(\"" + uri + "\") annotation at: " + prevMethodName + " and " + currMethodName;
            LOGGER.fatal(() -> message);
            throw new UnsupportedOperationException(message);
        }
        getMappings.put(uri, method);
        uriMappings.add(uri);
    }

    /**
     * @param uri
     * @return
     */
    public static boolean isMappedPath(final String uri) {
        if (CharSequenceUtils.isBlank(uri)) {
            return uriMappings.contains("/");
        }
        return uriMappings.contains(uri.trim());
    }
 
    /**
     * @param uri
     * @return
     * @throws Exception
     */
    public static String get(final HttpRequest httpRequest) throws Exception {
        final Method method = getMappings.get(httpRequest.getPathInfo());
        return JsonUtils.marshall(invoke(method));
    }

    /**
     * @param method
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static Object invoke(final Method method) throws IllegalAccessException, InvocationTargetException {
        ObjectUtils.requireNotNull(method, "method parameter is null");
        final Class<?> clazz = method.getDeclaringClass();
        final Object result = method.invoke(Optional.ofNullable(singletons.get(clazz)).orElse(ObjectUtils.newInstance(clazz)));
        LOGGER.trace(() -> "Object returned: " + result);
        return result;
    }
}