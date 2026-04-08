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
 * Example:
 * <pre>
 * {@literal @}BrandedType
 * public record UserID(int value) {}
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BrandedType {
}
