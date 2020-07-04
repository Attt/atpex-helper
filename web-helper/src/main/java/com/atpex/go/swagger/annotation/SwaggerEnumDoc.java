package com.atpex.go.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Show enum as {@link #index()} value </br>
 * used for swagger property or parameter
 *
 * @author atpex
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerEnumDoc {

    /**
     * enum id or index
     */
    String index() default "index";

    /**
     * enum description or display name
     */
    String name() default "name";
}
