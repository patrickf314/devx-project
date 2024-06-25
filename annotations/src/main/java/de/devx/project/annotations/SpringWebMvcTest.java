package de.devx.project.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for spring web MVC tests.
 * This annotation is ment for annotating test classes
 * which contain unit tests of spring web controllers.
 * The main aim of these test should be to check if the spring annotation
 * on the API are set correctly.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface SpringWebMvcTest {

    Class<?> controller();

    Class<?> service();

    Class<?>[] context() default {};

    String[] activeProfiles() default {};
}
