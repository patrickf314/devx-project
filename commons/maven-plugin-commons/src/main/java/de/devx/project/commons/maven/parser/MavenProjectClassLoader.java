package de.devx.project.commons.maven.parser;

import de.devx.project.commons.generator.mapper.JavaClassMapper;
import de.devx.project.commons.generator.model.JavaClassModel;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class MavenProjectClassLoader {

    private final ClassLoader classLoader;

    public MavenProjectClassLoader(MavenProject mavenProject) {
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
}
