package de.devx.project.commons.generator.io;

public interface JavaFileGenerator extends SourceFileGenerator {

    @Override
    default String fileName(String className, boolean withExtension) {
        if(withExtension) {
            return className + ".java";
        }

        return className;
    }

    @Override
    default String importPath(String currentPackage, String targetPackage) {
        return targetPackage;
    }
}
