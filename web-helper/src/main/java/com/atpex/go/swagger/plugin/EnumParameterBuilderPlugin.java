package com.atpex.go.swagger.plugin;

import com.atpex.go.swagger.annotation.SwaggerEnumDoc;
import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Joiner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.atpex.go.common.Constants.*;

/**
 * Show enum declaring {@link SwaggerEnumDoc} as {@link SwaggerEnumDoc#index()} value </br>
 * used for swagger parameters
 *
 * @author atpex
 * @since 1.0
 */
public class EnumParameterBuilderPlugin extends Plugin implements ParameterBuilderPlugin, OperationBuilderPlugin {

    private static final Joiner joiner = Joiner.on(DELIMITER1);

    @Override
    public void apply(ParameterContext context) {
        Class<?> type = context.resolvedMethodParameter().getParameterType().getErasedType();

        boolean isArray;
        boolean isCollection;

        if (context.resolvedMethodParameter().getParameterType().getTypeParameters().size() > 0) {
            type = context.resolvedMethodParameter().getParameterType().getTypeParameters().get(0).getErasedType();
        }
        if (isArray = type.isArray()) {
            type = type.getComponentType();
        }
        if (isCollection = Collection.class.isAssignableFrom(type)) {
            ParameterizedType superclass = (ParameterizedType) type.getGenericInterfaces()[0];
            type = (Class) superclass.getActualTypeArguments()[0];
        }
        if (Enum.class.isAssignableFrom(type)) {
            SwaggerEnumDoc swaggerEnumDoc = AnnotationUtils.findAnnotation(type, SwaggerEnumDoc.class);
            if (swaggerEnumDoc != null) {

                Object[] enumConstants = type.getEnumConstants();
                List<String> valuesToDisplay = valuesToDisplay(enumConstants, swaggerEnumDoc, true);

                ParameterBuilder parameterBuilder = context.parameterBuilder();
                AllowableListValues values = new AllowableListValues(valuesToDisplay, LIST_TYPE_FOR_SWAGGER);
                parameterBuilder.allowableValues(values);
                if (isArray) {
                    parameterBuilder.scalarExample(new Integer[0]);
                } else if (isCollection) {
                    parameterBuilder.scalarExample(Arrays.asList());
                } else {
                    parameterBuilder.scalarExample(0);
                }
            }
        }
    }


    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    @Override
    public void apply(OperationContext context) {
        OperationBuilder operationBuilder = context.operationBuilder();
        Field parametersField = ReflectionUtils.findField(operationBuilder.getClass(), PARAMETERS_FIELD_NAME_FOR_SWAGGER);
        ReflectionUtils.makeAccessible(parametersField);
        List<Parameter> list = (List<Parameter>) ReflectionUtils.getField(parametersField, operationBuilder);

        list.forEach(parameter -> {
            ResolvedType parameterType = parameter.getType().get();
            Class<?> clazz = parameterType.getErasedType();

            if (clazz.isArray()) {
                clazz = clazz.getComponentType();
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                ParameterizedType superclass = (ParameterizedType) clazz.getGenericInterfaces()[0];
                Type actualTypeArgument = superclass.getActualTypeArguments()[0];
                if (actualTypeArgument instanceof Class) {
                    clazz = (Class<?>) actualTypeArgument;
                }
            }

            if (Enum.class.isAssignableFrom(clazz)) {
                SwaggerEnumDoc swaggerEnumDoc = AnnotationUtils.findAnnotation(clazz, SwaggerEnumDoc.class);
                if (swaggerEnumDoc != null) {
                    Object[] enumConstants = clazz.getEnumConstants();

                    List<String> valuesToDisplay = valuesToDisplay(enumConstants, swaggerEnumDoc, false);

                    Field description = ReflectionUtils.findField(parameter.getClass(), MODEL_DESCRIPTION_FIELD_NAME_FOR_SWAGGER);
                    ReflectionUtils.makeAccessible(description);
                    Object field = ReflectionUtils.getField(description, parameter);
                    ReflectionUtils.setField(description, parameter, field + DELIMITER1 + joiner.join(valuesToDisplay));

                }
            }
        });

    }

}
