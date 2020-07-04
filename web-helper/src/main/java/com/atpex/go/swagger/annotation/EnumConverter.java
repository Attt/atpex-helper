package com.atpex.go.swagger.annotation;

import java.lang.annotation.*;

/**
 * Declared by the method converting enum index or enum id to enum object
 *
 * @author atpex
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumConverter {
}
