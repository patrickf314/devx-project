package de.devx.project.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or record as a branded type — a wrapper around a single primitive or String value.
 * <p>
 * Branded types are transparent over HTTP: a field of type {@code UserID} is serialized as its
 * underlying primitive value (e.g., {@code int}) rather than as a JSON object. The annotation
 * processor and code generators will unwrap the branded type to its underlying type automatically.
 * <p>
 * The underlying type is determined in the following order:
 * <ol>
 *   <li>If {@link #type()} is set explicitly, that type is used.</li>
 *   <li>Otherwise the class/record must have exactly one non-static field; its type is used.</li>
 * </ol>
 * <p>
 * Example (inferred from field):
 * <pre>
 * {@literal @}BrandedType
 * public record UserID(int value) {}
 * </pre>
 * Example (explicit type):
 * <pre>
 * {@literal @}BrandedType(type = String.class)
 * public class CorrelationId { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BrandedType {

    /**
     * The underlying type of this branded type.
     * Defaults to {@code void.class}, which means the type is inferred from the single field.
     */
    Class<?> type() default void.class;
}
