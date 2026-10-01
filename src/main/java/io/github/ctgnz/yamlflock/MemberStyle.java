package io.github.ctgnz.yamlflock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds a style annotation on the member that declares a property, so that {@link YamlFlowStyle} and {@link YamlBlockStyle} can be written where the property is rather than only
 * on the type that owns it.
 * <p>
 * The generator only ever has a property <em>name</em> and the value that owns it, so the member has to be looked up. A field of that name is tried first, then a zero-argument
 * method that reads as its accessor - {@code genres}, {@code getGenres} or {@code isGenres} - which covers a record component and a bean getter alike. Superclasses are searched
 * too, because a property may be declared further up.
 * <p>
 * Results are cached per class and property. Reflection on every value written would be a real cost for a large document, and the answer cannot change at runtime.
 */
final class MemberStyle {

    private static final Map<Key, Annotation> RESOLVED = new ConcurrentHashMap<>();
    private static final Annotation ABSENT = new Absent();

    private MemberStyle() {
    }

    /** The annotation of the given type on the member declaring {@code property} of {@code owner}, or null where there is none. */
    @SuppressWarnings("unchecked")
    static <A extends Annotation> A on(Class<?> owner, String property, Class<A> type) {
        if (owner == null || property == null) {
            return null;
        }
        Annotation found = RESOLVED.computeIfAbsent(new Key(owner, property, type), key -> {
            Annotation annotation = search(key.owner(), key.property(), key.type());
            return annotation == null ? ABSENT : annotation;
        });
        return found == ABSENT ? null : (A) found;
    }

    private static <A extends Annotation> A search(Class<?> owner, String property, Class<A> type) {
        for (Class<?> declaring = owner; declaring != null && declaring != Object.class; declaring = declaring.getSuperclass()) {
            A onField = fromField(declaring, property, type);
            if (onField != null) {
                return onField;
            }
            A onAccessor = fromAccessor(declaring, property, type);
            if (onAccessor != null) {
                return onAccessor;
            }
        }
        return null;
    }

    private static <A extends Annotation> A fromField(Class<?> declaring, String property, Class<A> type) {
        try {
            Field field = declaring.getDeclaredField(property);
            return field.getAnnotation(type);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static <A extends Annotation> A fromAccessor(Class<?> declaring, String property, Class<A> type) {
        for (Method method : declaring.getDeclaredMethods()) {
            if (method.getParameterCount() == 0 && isAccessorFor(method.getName(), property)) {
                A annotation = method.getAnnotation(type);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }

    private static boolean isAccessorFor(String method, String property) {
        if (method.equals(property)) {
            return true;
        }
        String capitalised = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        return method.equals("get" + capitalised) || method.equals("is" + capitalised);
    }

    private record Key(Class<?> owner, String property, Class<? extends Annotation> type) {
    }

    /** A cached "nothing here", so an absent annotation is not re-derived on every write. */
    private record Absent() implements Annotation {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Absent.class;
        }

    }

}
