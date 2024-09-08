package de.devx.project.commons.processor.utils;

import de.devx.project.commons.generator.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public final class ExceptionUtils {

    private ExceptionUtils() {
        // No instances
    }

    public static IllegalArgumentException unexpectedTypeMirrorException(Logger logger, Element element, TypeMirror type) {
        var message = "Unexpected type mirror " + type + "(" + type.getClass().getName() + ")";
        logger.error(message, element);
        return new IllegalArgumentException(message);
    }

}
