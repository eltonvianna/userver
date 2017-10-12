/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 18/09/2017
 */
@FunctionalInterface
public interface HttpRequestHandler {
    
    /**
     * @param httpRequest
     * @throws Exception
     */
    void handle(final HttpRequest httpRequest) throws Exception;
}