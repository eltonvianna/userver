/*
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import com.esv.net.utils.MimeTypeUtils;
import com.esv.utile.logging.Logger;
import com.esv.utile.utils.IOUtils;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 18/09/2017
 */
public class HttpResponse {

    private static final Logger LOGGER = Logger.getLogger(HttpResponse.class);
    
    /**
     * @param data
     */
    public static void ok(final String data, final String contentType) {
        HttpResponse.write(data, contentType, 200, "OK");
    }
    
    /**
     * 
     * @param data
     * @param contentType
     * @param responseMessage
     */
    public static void notFound(final String data, final String contentType, final String responseMessage) {
        HttpResponse.write(data, contentType, 404, responseMessage);
    }
    
    /**
     * 
     * @param data
     * @param contentType
     * @param responseMessage
     */
    public static void notAllowed(final String data, final String contentType, final String responseMessage) {
        HttpResponse.write(data, contentType, 405, responseMessage);
    }
    
    /**
     * 
     * @param data
     * @param contentType
     * @param responseMessage
     */
    public static void serverError(final String data, final String contentType, final String responseMessage) {
        HttpResponse.write(data, contentType, 500, responseMessage);
    }
    
    /**
     * @param data
     * @param responseCode
     * @param responseMessage
     */
    private static void write(final String data, final String contentType, final int responseCode, final String responseMessage) {
        final PrintWriter out = new PrintWriter(HttpRequest.getCurrentRrequest().getOutputStream(), true);
        out.println("HTTP/1.1 " + responseCode + " " + responseMessage);
        out.println("Allow: GET");
        out.println("Content-type: " + contentType);
        out.println("Content-length: " + data.length());
        out.println("");
        out.println(data);
        LOGGER.trace(() -> "Write data: " + data + ", Status code: " + responseCode + ", Content-type: " + contentType
                + ", Content-length: " + data.length());
    }
    
    /**
     * @param inputStream
     * @param resourceName
     * @param cacheMaxAge
     * @throws IOException
     */
    public static void write(final InputStream inputStream, final String resourceName, final int cacheMaxAge) throws IOException {
       HttpResponse.write(IOUtils.toByteArray(inputStream), resourceName, cacheMaxAge);
    }
    
    /**
     * @param data
     * @param resourceName
     * @param cacheMaxAge
     * @throws IOException
     */
    public static void write(final byte[] data, final String resourceName, final int cacheMaxAge) throws IOException {
        final DataOutputStream out = new DataOutputStream(HttpRequest.getCurrentRrequest().getOutputStream());
        out.writeBytes("HTTP/1.1 200 OK\n");
        out.writeBytes("Allow: GET\n");
        final String contentType = MimeTypeUtils.get(resourceName);
        out.writeBytes("Content-type: " + contentType + "\n");
        out.writeBytes("Content-length: " + data.length +"\n");
        final String cacheControl = getCacheControl(cacheMaxAge);
        out.writeBytes("Cache-Control: max-age=" + cacheControl + "\n");
        out.writeBytes("\n");
        out.write(data);
        out.flush();
        LOGGER.trace(() -> "Write data: " + resourceName + ", Status code: 200, Content-type: " + contentType
                + ", Content-length: " + data.length + ", Cache-Control: max-age=" + cacheControl);
    }

    /**
     * @param cacheMaxAge
     * @return
     */
    private static String getCacheControl(final int cacheMaxAge) {
        return cacheMaxAge > 0 ? String.valueOf(cacheMaxAge) : "0, no-cache, must-revalidate, proxy-revalidate";
    }
}