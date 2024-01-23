package de.devx.project.commons.maven.logging;

import de.devx.project.commons.generator.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;

import javax.lang.model.element.Element;

@RequiredArgsConstructor
public class MavenLogger implements Logger {

    private final Log log;

    @Override
    public void error(String message, Element element) {
        log.error(elementPrefix(element) + message);
    }

    @Override
    public void warn(String message, Element element) {
        log.warn(elementPrefix(element) + message);
    }

    private String elementPrefix(Element element) {
        if(element == null) {
            return "";
        }

        return element.getSimpleName() + ": ";
    }
}
