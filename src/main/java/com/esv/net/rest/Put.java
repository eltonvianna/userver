/* 
 * © 2017 Springer Nature 
 */
package com.esv.net.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 04/10/2017
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Put {
    
    String value();
}