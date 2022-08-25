package api.maven.plugin.processor.spring.mapper;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.stream.Collectors;

public final class AnnotationValueMapper {

    private AnnotationValueMapper() {
        // No instances
    }

    public static List<String> mapAnnotationValueToStringArray(AnnotationValue annotationValue) {
        var value = annotationValue.getValue();
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("Cannot map annotation value of type " + value.getClass().getName() + " to a string array");
        }

        return ((List<?>) value).stream()
                .map(item -> {
                    if (!(item instanceof AnnotationValue)) {
                        throw new IllegalArgumentException("Unexpected list entry " + item.getClass().getName() + " in annotation value");
                    }

                    return mapAnnotationValueToString((AnnotationValue) item);
                })
                .collect(Collectors.toList());
    }

    public static String mapAnnotationValueToString(AnnotationValue annotationValue) {
        var value = annotationValue.getValue();
        if (value instanceof String) {
            return (String) value;
        } else if(value instanceof VariableElement) {
            return ((VariableElement) value).getSimpleName().toString();
        } else {
            throw new IllegalArgumentException("Cannot map annotation value of type " + value.getClass().getSimpleName() + " to a string");
        }
    }

    public static boolean mapAnnotationValueToBoolean(AnnotationValue annotationValue) {
        var value = annotationValue.getValue();
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            throw new IllegalArgumentException("Cannot map annotation value of type " + value.getClass().getSimpleName() + " to a boolean");
        }
    }

}
