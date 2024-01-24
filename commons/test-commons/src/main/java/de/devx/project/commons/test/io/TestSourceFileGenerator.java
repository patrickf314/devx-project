package de.devx.project.commons.test.io;

import de.devx.project.commons.generator.io.SourceFileGenerator;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestSourceFileGenerator implements SourceFileGenerator {

    private final Map<String, StringWriter> sourceFiles = new HashMap<>();

    @Override
    public Writer createSourceFile(String packageName, String className) {
        return sourceFiles.computeIfAbsent(packageName + "." + className, s -> new StringWriter());
    }

    @Override
    public String fileName(String className) {
        return className;
    }

    @Override
    public String importPath(String currentPackage, String targetPackage) {
        return targetPackage;
    }

    public Optional<String> getFileContent(String packageName, String className) {
        return Optional.ofNullable(sourceFiles.get(packageName + "." + className))
                .map(StringWriter::toString);
    }
}
