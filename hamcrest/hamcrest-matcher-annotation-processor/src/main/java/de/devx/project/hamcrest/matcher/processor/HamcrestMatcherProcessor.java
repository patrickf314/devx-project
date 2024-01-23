package de.devx.project.hamcrest.matcher.processor;

import de.devx.project.commons.generator.io.SourceFileGenerator;
import de.devx.project.commons.generator.logging.Logger;
import de.devx.project.commons.processor.io.JavaSourceFileGenerator;
import de.devx.project.commons.processor.logging.JavaProcessorLogger;
import de.devx.project.hamcrest.matcher.generator.HamcrestMatcherGenerator;
import de.devx.project.hamcrest.matcher.processor.mapper.HamcrestMatcherElementMapper;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class HamcrestMatcherProcessor extends AbstractProcessor {

    private Logger logger;
    private SourceFileGenerator fileGenerator;
    private HamcrestMatcherGenerator generator;
    private HamcrestMatcherElementMapper mapper;

    public Logger getLogger() {
        if (logger == null) {
            logger = new JavaProcessorLogger(processingEnv.getMessager());
        }

        return logger;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        for (var annotation : annotations) {
            generateMatchers(roundEnv.getElementsAnnotatedWith(annotation));
        }
        return true;
    }

    public SourceFileGenerator getFileGenerator() {
        if (fileGenerator == null) {
            fileGenerator = new JavaSourceFileGenerator(processingEnv.getFiler());
        }

        return fileGenerator;
    }

    public HamcrestMatcherGenerator getGenerator() {
        if (generator == null) {
            generator = new HamcrestMatcherGenerator(getFileGenerator());
        }
        return generator;
    }

    public HamcrestMatcherElementMapper getMapper() {
        if (mapper == null) {
            mapper = new HamcrestMatcherElementMapper(getLogger());
        }
        return mapper;
    }

    public void generateMatchers(Collection<? extends Element> elements) {
        elements.forEach(this::generateMatcher);
    }

    public void generateMatcher(Element element) {
        var matcher = getMapper().mapToMatcher(element);
        if (matcher.isEmpty()) {
            return;
        }

        try {
            getGenerator().generate(matcher.get());
        } catch (IOException e) {
            getLogger().error("Failed to generate hamcrest matcher: " + e.getMessage(), element);
        }
    }
}
