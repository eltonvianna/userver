/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.esv.utile.utils.CharSequenceUtils;

/**
 * 
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 28/09/2017
 */
@SuppressWarnings("serial")
public final class MimeTypeUtils {

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String APPLICATION_X_FONT_TTF = "application/x-font-ttf";
    public static final String IMAGE_PNG = "image/png";
    public static final String TEXT_JAVASCRIPT = "text/javascript";
    public static final String TEXT_CSS = "text/css";
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_PLAIN = "text/plain";
    
    private static final Map<String, String> mimeTypes;
    
    /**
     * Suppressing default constructor for non instantiability
     */
    private MimeTypeUtils() {
        throw new AssertionError("Suppress default constructor for non instantiability");
    }
    
    static {
        mimeTypes = Collections.unmodifiableMap(new HashMap<String,String>() {{
          put("html", TEXT_HTML);
          put("css", TEXT_CSS);
          put("js", TEXT_JAVASCRIPT);
          put("png", IMAGE_PNG);
          put("properties", TEXT_PLAIN);
          put("ttf", APPLICATION_X_FONT_TTF);
          put("json", APPLICATION_JSON);
        }});
    }
    
    /**
     * @param file
     * @return
     */
    public static String get(final String resourceName) {
        if (CharSequenceUtils.isBlank(resourceName)) {
            return APPLICATION_OCTET_STREAM;
        }
        final int idx = resourceName.lastIndexOf(".");
        if (idx == -1) {
            return APPLICATION_OCTET_STREAM;
        }
        return mimeTypes.getOrDefault(resourceName.substring(idx + 1).trim().toLowerCase(), APPLICATION_OCTET_STREAM);
    }
}