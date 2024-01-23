package de.devx.project.commons.processor.io;

import de.devx.project.commons.generator.io.SourceFileGenerator;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.io.Writer;

@RequiredArgsConstructor
public class JavaSourceFileGenerator implements SourceFileGenerator {

    private final Filer filer;

    @Override
    public Writer createSourceFile(String packageName, String className) throws IOException {
        return filer.createSourceFile(packageName + "." + className).openWriter();
    }

    @Override
    public String fileName(String className) {
        return className + ".java";
    }
}
