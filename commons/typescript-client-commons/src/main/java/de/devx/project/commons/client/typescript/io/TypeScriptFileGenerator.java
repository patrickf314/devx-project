package de.devx.project.commons.client.typescript.io;

import de.devx.project.commons.client.typescript.utils.TypeScriptUtils;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@RequiredArgsConstructor
public class TypeScriptFileGenerator implements SourceFileGenerator {

    private final String outputDirectory;

    @Override
    public Writer createSourceFile(String packageName, String className) throws IOException {
        var directory = packageDirectory(packageName);
        var fileName = fileName(className);
        var file = ensureFileExists(new File(directory, fileName));
        return new FileWriter(file);
    }

    @Override
    public String fileName(String className) {
        if (className.endsWith("ServiceAPI") || className.endsWith("ServiceApi")) {
            return TypeScriptUtils.toLowerCaseName(className.substring(0, className.length() - 10)) + ".service";
        }

        if (className.endsWith("DTO") || className.endsWith("Dto")) {
            return TypeScriptUtils.toLowerCaseName(className.substring(0, className.length() - 3) + ".dto");
        }

        if (className.endsWith("Type")) {
            return TypeScriptUtils.toLowerCaseName(className.substring(0, className.length() - 4)) + ".type";
        }

        return TypeScriptUtils.toLowerCaseName(className);
    }

    private File packageDirectory(String packageName) throws IOException {
        var directory = new File(outputDirectory, packageName.replace('.', File.separatorChar));
        return ensurePackageDirectoryExists(directory, packageName);
    }

    private File ensurePackageDirectoryExists(File directory, String packageName) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory for package " + packageName + ": " + directory.getAbsolutePath());
        }
        return directory;
    }


    private File ensureFileExists(File file) throws IOException {
        if (!file.isFile() && !file.createNewFile()) {
            throw new IOException("Failed to create file " + file.getAbsolutePath());
        }

        return file;
    }
}
