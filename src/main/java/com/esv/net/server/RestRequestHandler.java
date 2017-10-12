/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.esv.net.HttpRequest;
import com.esv.net.HttpRequestHandler;
import com.esv.net.HttpResponse;
import com.esv.net.utils.MimeTypeUtils;
import com.esv.utile.utils.JsonUtils;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 18/09/2017
 */
public class RestRequestHandler implements HttpRequestHandler {

    private static final Logger LOGGER = Logger.getGlobal();
    
    /*
     * (non-Javadoc)
     * 
     * @see com.esv.net.server.RequestHandler#handleRequest(com.esv.net.server.HttpRequestImpl)
     */
    @Override
    public void handle(final HttpRequest httpRequest) throws Exception {
        if (httpRequest.isRestRequest()) {
            LOGGER.fine(() -> "Handling rest service request: " + httpRequest.getRequestURI());
            String json = JsonUtils.empty();
            if (RestServiceInvoker.isMappedPath(httpRequest.getPathInfo())) {
                try {
                    switch (httpRequest.getRequestMethod()) {
                    case "GET":
                        json = RestServiceInvoker.get(httpRequest);
                        break;
                    case "DELETE":
                    case "POST":
                    case "PUT":
                    default:
                        break;
                    }
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                    json = JsonUtils.createBuilder("message", "Internal server error").build();
                }
            } else {
                final String message = "Invalid endpoint: " + httpRequest.getPathInfo();
                LOGGER.warning(() -> message);
                json = JsonUtils.createBuilder("message", message).build();
            }
            //
           final String response = json;
           LOGGER.fine(() -> "Returning json response: " + response);
           HttpResponse.ok(json, MimeTypeUtils.APPLICATION_JSON);
        }
    }
}