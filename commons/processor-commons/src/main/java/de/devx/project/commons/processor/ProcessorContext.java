package de.devx.project.commons.processor;

import javax.annotation.processing.ProcessingEnvironment;

public final class ProcessorContext {

    private static ProcessingEnvironment processingEnvironment;

    public static void init(ProcessingEnvironment processingEnvironment) {
        ProcessorContext.processingEnvironment = processingEnvironment;
    }

    public static ProcessingEnvironment getProcessingEnvironment() {
        if (processingEnvironment == null) {
            throw new IllegalStateException("Processor context has not been initialized");
        }

        return processingEnvironment;
    }
}
