package de.devx.project.commons.generator.io;

import java.io.IOException;
import java.io.Writer;

public interface SourceFileGenerator {

    Writer createSourceFile(String packageName, String className) throws IOException;

    String fileName(String className);
}
