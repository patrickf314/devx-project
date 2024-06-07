package de.devx.project.commons.generator.io;

import java.io.IOException;
import java.io.Writer;

public interface SourceFileGenerator {

    Writer createSourceFile(String packageName, String className) throws IOException;

    default String fileName(String className){
        return fileName(className, true);
    }

    String fileName(String className, boolean withExtension);

    String importPath(String currentPackage, String targetPackage);
}
