package de.devx.project.spring.webmvc.test.processor.data;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.DeclaredType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.devx.project.commons.processor.utils.AnnotationElementUtils.extractFieldsFromAnnotationMirror;

public class SpringWebMvcTestAnnotation {

    private static final String CONTEXT = "context";
    private static final String ACTIVE_PROFILES = "activeProfiles";
    private static final String SERVICE = "service";
    private static final String CONTROLLER = "controller";

    private final Map<String, AnnotationValue> values;

    public SpringWebMvcTestAnnotation(AnnotationMirror mirror) {
        this.values = extractFieldsFromAnnotationMirror(mirror);
    }

    public DeclaredType controller() {
        return valueAsDeclaredType(CONTROLLER);
    }

    public DeclaredType service() {
        return valueAsDeclaredType(SERVICE);
    }

    public List<DeclaredType> context() {
        return valueAsList(CONTEXT).stream()
                .map(context -> context instanceof AnnotationValue value ? castToDeclaredType(CONTEXT, value.getValue()) : castToDeclaredType(CONTEXT, context))
                .toList();
    }

    public List<String> activeProfiles() {
        return valueAsList(ACTIVE_PROFILES).stream()
                .map(AnnotationValue.class::cast)
                .map(profile -> castToString(ACTIVE_PROFILES, profile.getValue()))
                .toList();
    }

    private DeclaredType valueAsDeclaredType(String name) {
        var value = values.get(name);
        if (value == null) {
            return null;
        }

        return castToDeclaredType(name, value.getValue());
    }

    private List<?> valueAsList(String name) {
        var value = values.get(name);
        if (value == null) {
            return Collections.emptyList();
        }

        return castToList(name, value.getValue());
    }

    private DeclaredType castToDeclaredType(String name, Object value) {
        if (value instanceof DeclaredType declaredType) {
            return declaredType;
        }

        throw castException(value, "declared type", name);
    }

    private List<?> castToList(String name, Object value) {
        if (value instanceof List<?> list) {
            return list;
        }

        throw castException(value, "list", name);
    }

    private String castToString(String name, Object value) {
        if (value instanceof String string) {
            return string;
        }

        throw castException(value, "string", name);
    }

    private RuntimeException castException(Object value, String targetType, String name) {
        var message = "Failed to cast " + (value == null ? "null" : value.getClass().getName()) + " to a " + targetType + ". Value found in @SpringWebMvcTest." + name;
        return new IllegalArgumentException(message);
    }
}
