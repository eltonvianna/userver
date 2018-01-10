/*
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net.server;

import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import com.esv.net.HttpRequest;
import com.esv.net.HttpRequestHandler;
import com.esv.net.HttpResponse;
import com.esv.net.utils.MimeTypeUtils;
import com.esv.utile.logging.Logger;
import com.esv.utile.utils.ObjectUtils;
import com.esv.utile.utils.PropertiesUtils;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 19/09/2017
 */
final class SocketHandler {

    private static final Logger LOGGER = Logger.getLogger(SocketHandler.class);
    private static final List<HttpRequestHandler> httpRequestHandlers;

    /**
     * <p>
     * Create a singleton and immutable request handlers list based on
     * request.handlers configuration required to handle the application
     * requests.
     * </p>
     */
    static {
        try {
            final String[] handlers = PropertiesUtils.getRequiredProperty("request.handlers").split(",");
            httpRequestHandlers = ObjectUtils.newInstances(handlers);
            LOGGER.debug(() -> "Successfully load the request handlers: " + httpRequestHandlers);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * @param method a HTTP method
     * @return true if the given HTTP method is allowed
     */
    protected static boolean isAllowedMethod(final String method) {
        switch (method) {
        case "GET":
            return true;
        case "DELETE":
        case "OPTIONS":
        case "POST":
        case "PUT":
        default:
            return false;
        }
    }
    
    /**
     * @param method a HTTP method
     * @return true if the given HTTP method is not allowed
     */
    protected static boolean isNotAllowedMethod(final String method) {
        return ! SocketHandler.isAllowedMethod(method);
    }

    /**
     * <p>
     * Handle the given socket connection, checking is an allowed request
     * method, creating a immutable instance of {@link HttpRequest} and delegating
     * it to the {@link SocketHandler#httpRequestHandlers} and closing the resources
     * when handle finish
     * </p>
     * 
     * @param request
     */
    public static void handle(final Socket socket) {
        try (final OutputStream outputStream = socket.getOutputStream()) {
            // creating a new request object
            final HttpRequest httpRequest = HttpRequest.newInstance(socket);
            LOGGER.trace(() -> "HttpRequest content: " + httpRequest);
            // check if is an allowed request method
            final String requestMethod = httpRequest.getRequestMethod();
            if (SocketHandler.isNotAllowedMethod(requestMethod)) {
                final String message = "Method Not Allowed: " + requestMethod;
                LOGGER.error(message);
                HttpResponse.notAllowed(message, MimeTypeUtils.TEXT_PLAIN, message);
                return;
            }
            try {
                for (final HttpRequestHandler httpRequestHandler : SocketHandler.httpRequestHandlers) {
                    httpRequestHandler.handle(httpRequest);
                }
            } catch (Exception e) {
                final String message = "Internal Server Error";
                LOGGER.fatal(message, e);
                HttpResponse.serverError(message, MimeTypeUtils.TEXT_PLAIN, message);
            }
        } catch (Throwable t) {
                LOGGER.fatal("Unexpected error. Could not send a response. Please try again later", t);
            return;
        }
    }
}