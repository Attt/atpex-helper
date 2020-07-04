package com.atpex.go.swagger.plugin;

import com.atpex.go.swagger.annotation.SwaggerEnumDoc;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.core.annotation.AnnotationUtils;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.Types;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;

import java.util.Collection;
import java.util.List;

import static com.atpex.go.common.Constants.INT_TYPE_FOR_SWAGGER;
import static com.atpex.go.common.Constants.LIST_TYPE_FOR_SWAGGER;

/**
 * Show enum declaring {@link SwaggerEnumDoc} as {@link SwaggerEnumDoc#index()} value </br>
 * used for swagger parameters
 *
 * @author atpex
 * @since 1.0
 */
public class EnumExpendedParameterBuilderPlugin extends Plugin implements ExpandedParameterBuilderPlugin {

    private final TypeResolver typeResolver = new TypeResolver();

    @Override
    public void apply(ParameterExpansionContext context) {
        Class<?> type = context.getFieldType().getErasedType();

        if (type.isArray()) {
            type = type.getComponentType();
        }
        if (Collection.class.isAssignableFrom(type)) {
            type = context.getFieldType().getTypeParameters().get(0).getErasedType();
        }
        if (Enum.class.isAssignableFrom(type)) {
            SwaggerEnumDoc swaggerEnumDoc = AnnotationUtils.findAnnotation(type, SwaggerEnumDoc.class);
            if (swaggerEnumDoc != null) {

                Object[] enumConstants = type.getEnumConstants();
                List<String> valuesToDisplay = valuesToDisplay(enumConstants, swaggerEnumDoc, true);

                ParameterBuilder parameterBuilder = context.getParameterBuilder();
                AllowableListValues values = new AllowableListValues(valuesToDisplay, LIST_TYPE_FOR_SWAGGER);
                parameterBuilder.allowableValues(values);

                parameterBuilder.type(typeResolver.resolve(Integer[].class));


                String typeName;
                ModelReference itemModel = null;
                ResolvedType resolved = context.getFieldType();
                if (Collections.isContainerType(resolved)) {
                    ResolvedType elementType = Collections.collectionElementType(resolved);
                    String itemTypeName = Types.typeNameFor(elementType.getErasedType());
                    if (Enum.class.isAssignableFrom(elementType.getErasedType())) {
                        itemTypeName = INT_TYPE_FOR_SWAGGER;
                    }

                    typeName = Collections.containerType(resolved);
                    itemModel = new ModelRef(itemTypeName, values);
                    parameterBuilder.allowMultiple(Collections.isContainerType(resolved)).type(resolved).modelRef(new ModelRef(typeName, itemModel)).order(0);
                } else if (Enum.class.isAssignableFrom(resolved.getErasedType())) {
                    typeName = INT_TYPE_FOR_SWAGGER;
                    parameterBuilder.allowMultiple(Collections.isContainerType(resolved)).type(resolved).modelRef(new ModelRef(typeName, itemModel)).order(0);
                }

            }
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}
