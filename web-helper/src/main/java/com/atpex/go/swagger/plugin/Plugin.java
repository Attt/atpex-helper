package com.atpex.go.swagger.plugin;

import com.atpex.go.swagger.annotation.SwaggerEnumDoc;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.atpex.go.common.Constants.DELIMITER0;

/**
 *
 * @author atpex
 * @since 1.0
 */
class Plugin {

    List<String> valuesToDisplay(Object[] enumConstants, SwaggerEnumDoc swaggerEnumDoc, boolean onlyValue) {
        return Arrays.stream(enumConstants).filter(Objects::nonNull).map(item -> {
            Class<?> currentClass = item.getClass();

            Field indexField = ReflectionUtils.findField(currentClass, swaggerEnumDoc.index());
            ReflectionUtils.makeAccessible(indexField);
            Object value = ReflectionUtils.getField(indexField, item);

            if (onlyValue) {
                return value.toString();
            }

            Field descField = ReflectionUtils.findField(currentClass, swaggerEnumDoc.name());
            ReflectionUtils.makeAccessible(descField);
            Object desc = ReflectionUtils.getField(descField, item);
            return value + DELIMITER0 + desc;

        }).collect(Collectors.toList());
    }
}
