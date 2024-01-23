package de.devx.project.commons.generator.logging;

import javax.lang.model.element.Element;

public interface Logger {

    default void error(String message) {
        error(message, null);
    }

    void error(String message, Element element);

    default void warn(String message) {
        warn(message, null);
    }

    void warn(String message, Element element);
}
