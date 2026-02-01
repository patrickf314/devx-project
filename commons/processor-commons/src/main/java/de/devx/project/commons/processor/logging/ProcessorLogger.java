package de.devx.project.commons.processor.logging;

import de.devx.project.commons.generator.logging.Logger;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

@RequiredArgsConstructor
public class ProcessorLogger implements Logger {

    private final Messager messager;

    @Override
    public void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }


    @Override
    public void warn(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, message, element);
    }

    @Override
    public void info(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
