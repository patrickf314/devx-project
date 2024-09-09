package de.devx.project.commons.client.typescript.io;

import de.devx.project.commons.client.typescript.utils.TypeScriptUtils;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public String fileName(String className, boolean withExtension) {
        var extension = withExtension ? ".ts" : "";

        if (className.endsWith("ServiceAPI") || className.endsWith("ServiceApi")) {
            return TypeScriptUtils.toLowerCaseName(className.substring(0, className.length() - 10)) + ".service" + extension;
        }

        if (className.endsWith("DTO") || className.endsWith("Dto")) {
            return TypeScriptUtils.toLowerCaseName(className.substring(0, className.length() - 3)) + ".dto" + extension;
        }

        if (className.endsWith("Type")) {
            return TypeScriptUtils.toLowerCaseName(className.substring(0, className.length() - 4)) + ".type" + extension;
        }

        return TypeScriptUtils.toLowerCaseName(className) + extension;
    }

    @Override
    public String importPath(String currentPackage, String targetPackage) {
        if (currentPackage.isEmpty() && targetPackage.isEmpty()) {
            return ".";
        }

        if (currentPackage.isEmpty()) {
            return "./" + targetPackage.replace('.', '/');
        }

        var currentPath = currentPackage.split("\\.");
        if (targetPackage.isEmpty()) {
            return Arrays.stream(currentPath).map(ignored -> "..").collect(Collectors.joining("/"));
        }

        var targetPath = targetPackage.split("\\.");

        var commonDepth = 0;
        for (var i = 0; i < Math.min(currentPath.length, targetPath.length); i++) {
            if (!currentPath[i].equals(targetPath[i])) {
                break;
            }
            commonDepth++;
        }

        var importPath = new StringBuilder();
        if (commonDepth == currentPath.length) {
            importPath.append(".");
        } else {
            importPath.append(IntStream.range(commonDepth, currentPath.length).mapToObj(i -> "..").collect(Collectors.joining("/")));
        }

        for (var i = commonDepth; i < targetPath.length; i++) {
            importPath.append("/").append(targetPath[i]);
        }

        return importPath.toString();
    }

    private File packageDirectory(String packageName) throws IOException {
        if (packageName.isEmpty()) {
            return new File(outputDirectory);
        }

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
