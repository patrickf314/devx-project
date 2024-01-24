package de.devx.project.commons.processor.io;

import de.devx.project.commons.generator.io.JavaFileGenerator;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.io.Writer;

@RequiredArgsConstructor
public class ProcessorJavaFileGenerator implements JavaFileGenerator {

    private final Filer filer;

    @Override
    public Writer createSourceFile(String packageName, String className) throws IOException {
        return filer.createSourceFile(packageName + "." + className).openWriter();
    }
}
