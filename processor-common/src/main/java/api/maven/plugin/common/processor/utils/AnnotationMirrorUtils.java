package api.maven.plugin.common.processor.utils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AnnotationMirrorUtils {

    private AnnotationMirrorUtils() {
        // No instances
    }

    public static Optional<AnnotationMirror> findAnnotationMirror(Element element, TypeElement annotation) {
        return findAnnotationMirror(element, annotation.asType());
    }

    public static Optional<AnnotationMirror> findAnnotationMirror(Element element, TypeMirror annotation) {
        return element.getAnnotationMirrors()
                .stream()
                .filter(mirror -> annotation.equals(mirror.getAnnotationType()))
                .findAny()
                .map(a -> a);
    }

    public static Map<String, AnnotationValue> extractFieldsFromAnnotationMirror(AnnotationMirror annotationMirror) {
        return annotationMirror.getElementValues()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getSimpleName().toString(), Map.Entry::getValue));
    }

    public static String getAnnotationName(AnnotationMirror mirror) {
        var type = mirror.getAnnotationType().asElement();
        if(type instanceof TypeElement) {
            return ((TypeElement) type).getQualifiedName().toString();
        }else{
            return type.getSimpleName().toString();
        }
    }
}
