/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net.server;

import java.io.InputStream;

import com.esv.net.HttpRequest;
import com.esv.net.HttpRequestHandler;
import com.esv.net.HttpResponse;
import com.esv.net.utils.MimeTypeUtils;
import com.esv.net.utils.WebResourceUtils;
import com.esv.utile.logging.Logger;
import com.esv.utile.utils.PropertiesUtils;
import com.esv.utile.utils.ResourceUtils;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 20/09/2017
 */
public class ResourceRequestHandler implements HttpRequestHandler {
    
    private static final Logger LOGGER = Logger.getLogger(ResourceRequestHandler.class);
    
    /**
     * @return the value of max-age 
     */
    private int cacheMaxAge() {
        return PropertiesUtils.getIntProperty("cache.maxAge", 0);
    }

    /*
     * (non-Javadoc)
     * @see com.esv.net.HttpRequestHandler#handle(com.esv.net.HttpRequest)
     */
    @Override
    public void handle(final HttpRequest httpRequest) throws Exception {
        if (httpRequest.isRestRequest()) {
            LOGGER.trace(() -> "Skipping handling the request URI: " + httpRequest.getRequestURI());
            return;
        }
        LOGGER.debug(() -> "Handling the resource request URI: " + httpRequest.getRequestURI());
        final String resourceName = WebResourceUtils.lookup(httpRequest, 1, WebResourceUtils.defaultPage());
        try (final InputStream inputStream = ResourceUtils.getAsStream(resourceName)) {
            if (null == inputStream) {
                final String message = "Resource not found: " + httpRequest.getPathInfo();
                LOGGER.error(() -> message);
                HttpResponse.notFound(message, MimeTypeUtils.TEXT_PLAIN, message);
                return;
            }
            HttpResponse.write(inputStream, resourceName, cacheMaxAge());
        }
    }
}