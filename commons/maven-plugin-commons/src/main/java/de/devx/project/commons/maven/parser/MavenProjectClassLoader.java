package de.devx.project.commons.maven.parser;

import de.devx.project.commons.generator.mapper.JavaClassMapper;
import de.devx.project.commons.generator.model.JavaClassModel;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MavenProjectClassLoader {

    private final MavenProject mavenProject;
    private final ClassLoader classLoader;

    public MavenProjectClassLoader(MavenProject mavenProject) {
        this.mavenProject = mavenProject;

        var urls = mavenProject.getArtifacts()
                .stream()
                .map(Artifact::getFile)
                .filter(file -> file != null && file.exists())
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new IllegalStateException("Failed to convert file to URL", e);
                    }
                })
                .toArray(URL[]::new);

        this.classLoader = new URLClassLoader(urls, MavenProjectClassLoader.class.getClassLoader());
    }

    public JavaClassModel getClass(String fullyQualifiedClassName) {
        try {
            return JavaClassMapper.mapToClassModel(classLoader.loadClass(fullyQualifiedClassName));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load class " + fullyQualifiedClassName, e);
        }
    }

    private Stream<JavaClassModel> streamAllClasses() {
        return mavenProject.getArtifacts()
                .stream()
                .map(Artifact::getFile)
                .filter(file -> file != null && file.exists())
                .flatMap(this::streamClassNames)
                .map(this::getClass);
    }

    private Stream<String> streamClassNames(File jarFile) {
        try (var zipFile = new ZipFile(jarFile)) {
            return zipFile.stream()
                    .map(ZipEntry::getName)
                    .filter(name -> name.endsWith(".class"))
                    .map(name -> name.substring(0, name.length() - 6).replace('/', '.'))
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to iterate jar file directory " + jarFile.getAbsolutePath(), e);
        }
    }
}
