package com.teles.lambda.meta.factory;

import java.lang.invoke.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LambdaMetaFactoryAccessor<T> {

    private static final Map<Class<?>, List<CachedMethodHandle>> cachedMetadata = new ConcurrentHashMap<>();

    private final Class<T> clazz;

    public LambdaMetaFactoryAccessor(Class<T> clazz) {
        this.clazz = clazz;

        cachedMetadata.computeIfAbsent(clazz, clz ->
                Arrays.stream(clz.getDeclaredFields())
                        .map(field -> {

                            try {

                                MethodHandles.Lookup lookup = MethodHandles.lookup();
                                CallSite site = LambdaMetafactory.metafactory(lookup,
                                        "apply",
                                        MethodType.methodType(Function.class),
                                        MethodType.methodType(Object.class, Object.class),
                                        lookup.findVirtual(clz, resolveGetter(field.getName()), MethodType.methodType(field.getType())),
                                        MethodType.methodType(field.getType(), clz));

                                return new CachedMethodHandle(field.getType(), site.getTarget());

                            } catch (Throwable t) {
                                throw new RuntimeException(t);
                            }

                        }).collect(Collectors.toList()));

    }

    public String toCsvString(T t) {
        return cachedMetadata.get(clazz)
                .stream()
                .map(c -> {
                    try {
                        return ((Function) c.getMethodHandle().invokeExact());
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                }).map(f -> f.apply(t).toString())
                .collect(Collectors.joining(", "));
    }

    private String resolveGetter(String field) {
        return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
    }

    static class CachedMethodHandle {

        Class clazz;

        MethodHandle methodHandle;

        public CachedMethodHandle(Class clazz, MethodHandle methodHandle) {
            this.clazz = clazz;
            this.methodHandle = methodHandle;
        }

        public Class getClazz() {
            return clazz;
        }

        public MethodHandle getMethodHandle() {
            return methodHandle;
        }
    }

}
