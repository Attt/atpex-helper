package com.atpex.go.swagger.plugin;


import com.atpex.go.swagger.annotation.SwaggerEnumDoc;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Optional;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import springfox.documentation.builders.ModelPropertyBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atpex.go.common.Constants.DELIMITER2;
import static com.atpex.go.common.Constants.MODEL_DESCRIPTION_FIELD_NAME_FOR_SWAGGER;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

/**
 * Show enum declaring {@link SwaggerEnumDoc} as {@link SwaggerEnumDoc#index()} value </br>
 * used for swagger model properties
 *
 * @author atpex
 * @since 1.0
 */
public class EnumModelPropertyBuilderPlugin extends Plugin implements ModelPropertyBuilderPlugin {

    @Override
    public void apply(ModelPropertyContext context) {
        Optional<BeanPropertyDefinition> optional = context.getBeanPropertyDefinition();
        if (!optional.isPresent()) {
            return;
        }

        AnnotatedField field = optional.get().getField();

        // fixme if list or collection is used, parameterized type is erased...
        if (field.getType().isContainerType() && !Map.class.isAssignableFrom(field.getType().getRawClass())) {

            addDescForEnum(context, field.getType().getContentType().getRawClass(), true);
        } else {
            final Class<?> fieldType = field.getRawType();
            addDescForEnum(context, fieldType, false);
        }

    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    private void addDescForEnum(ModelPropertyContext context, Class<?> fieldType, boolean forceArray) {
        boolean array;
        boolean collection;
        if (array = fieldType.isArray()) {
            fieldType = fieldType.getComponentType();
        }
        if (collection = Collection.class.isAssignableFrom(fieldType)) {
            ParameterizedType superclass = (ParameterizedType) fieldType.getGenericSuperclass();
            fieldType = (Class) superclass.getActualTypeArguments()[0];
        }
        if (Enum.class.isAssignableFrom(fieldType)) {
            SwaggerEnumDoc swaggerEnumDoc = AnnotationUtils.findAnnotation(fieldType, SwaggerEnumDoc.class);
            if (swaggerEnumDoc != null) {

                Object[] enumConstants = fieldType.getEnumConstants();

                List<String> displayValues = valuesToDisplay(enumConstants, swaggerEnumDoc, false);


                ModelPropertyBuilder builder = context.getBuilder();
                Field descField = ReflectionUtils.findField(builder.getClass(), MODEL_DESCRIPTION_FIELD_NAME_FOR_SWAGGER);
                ReflectionUtils.makeAccessible(descField);
                Object field = ReflectionUtils.getField(descField, builder);
                String joinText = (field == null ? EMPTY : field)
                        + " (" + String.join(DELIMITER2 + SPACE, displayValues) + ")";

                if (array || forceArray) {
                    builder.description(joinText).type(context.getResolver().resolve(Integer[].class)).example(new int[0]);
                } else if (collection) {
                    builder.description(joinText).type(context.getResolver().resolve(List.class, Integer.class)).example(Collections.EMPTY_LIST);
                } else {
                    builder.description(joinText).type(context.getResolver().resolve(Integer.class)).example(0);
                }
            }
        }

    }
}
