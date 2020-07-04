package com.atpex.go.swagger.plugin;

import com.atpex.go.swagger.annotation.EnumConverter;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atpex.go.common.Constants.ENUM_CONVERTER_DECLARED_BY_INCOMPATIBLE_METHOD_MESSAGE;

/**
 * Covert enum index or id to enum object with the method of enum object declaring {@link EnumConverter}
 *
 * @author atpex
 * @since 1.0
 */
public class EnumMvcConverterFactory implements ConverterFactory<String, Enum<?>> {

    private final ConcurrentMap<Class<? extends Enum<?>>, EnumMvcConverterHolder> holderMapper = new ConcurrentHashMap<>();

    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        EnumMvcConverterHolder holder = holderMapper.computeIfAbsent(targetType, EnumMvcConverterHolder::createHolder);
        return (Converter<String, T>) holder.converter;
    }


    @AllArgsConstructor
    static class EnumMvcConverterHolder {

        final EnumMvcConverter<?> converter;

        static EnumMvcConverterHolder createHolder(Class<?> targetType) {
            List<Method> methodList = MethodUtils.getMethodsListWithAnnotation(targetType, EnumConverter.class, false, true);
            if (CollectionUtils.isEmpty(methodList)) {
                return new EnumMvcConverterHolder(null);
            }
            Assert.isTrue(methodList.size() == 1, ENUM_CONVERTER_DECLARED_BY_INCOMPATIBLE_METHOD_MESSAGE);
            Method method = methodList.get(0);
            Assert.isTrue(Modifier.isStatic(method.getModifiers()), ENUM_CONVERTER_DECLARED_BY_INCOMPATIBLE_METHOD_MESSAGE);
            return new EnumMvcConverterHolder(new EnumMvcConverter<>(method));
        }

    }

    static class EnumMvcConverter<T extends Enum<T>> implements Converter<String, T> {

        private final Method method;

        EnumMvcConverter(Method method) {
            this.method = method;
            this.method.setAccessible(true);
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty()) {
                // reset the enum value to null.
                return null;
            }
            try {
                return (T) method.invoke(null, Integer.valueOf(source));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

    }
}
