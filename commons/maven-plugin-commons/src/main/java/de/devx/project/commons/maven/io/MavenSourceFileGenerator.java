package de.devx.project.commons.maven.io;

import de.devx.project.commons.generator.io.SourceFileGenerator;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@RequiredArgsConstructor
public class MavenSourceFileGenerator implements SourceFileGenerator {

    private final String outputDirectory;

    @Override
    public Writer createSourceFile(String packageName, String className) throws IOException {
        var file = new File(createPackageDirectory(packageName), fileName(className));
        return new FileWriter(file);
    }

    @Override
    public String fileName(String className) {
        return className + ".java";
    }

    private File createPackageDirectory(String packageName) throws IOException {
        var folder = new File(outputDirectory, packageName.replace('.', File.separatorChar));
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Failed to create package " + packageName + " in directory " + outputDirectory);
        }
        return folder;
    }
}
