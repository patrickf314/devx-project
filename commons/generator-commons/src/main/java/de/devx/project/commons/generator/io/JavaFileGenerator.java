package de.devx.project.commons.generator.io;

public interface JavaFileGenerator extends SourceFileGenerator {

    @Override
    default String fileName(String className) {
        return className + ".java";
    }

    @Override
    default String importPath(String currentPackage, String targetPackage) {
        return targetPackage;
    }
}
