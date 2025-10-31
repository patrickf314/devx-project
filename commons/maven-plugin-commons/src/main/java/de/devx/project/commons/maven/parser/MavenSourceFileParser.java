package de.devx.project.commons.maven.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.maven.mapper.MavenClassMapper;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class MavenSourceFileParser {

    private final MavenProject project;
    private final JavaParser parser;

    private final JavaSymbolSolver symbolSolver;

    public MavenSourceFileParser(MavenProject project) throws IOException, DependencyResolutionRequiredException {
        this.project = project;

        var typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver(false));

        for (var srcDir : project.getCompileSourceRoots()) {
            typeSolver.add(new JavaParserTypeSolver(srcDir));
        }

        for (var srcDir : project.getTestCompileSourceRoots()) {
            typeSolver.add(new JavaParserTypeSolver(srcDir));
        }

        for (var path : project.getCompileClasspathElements()) {
            var file = new File(path);
            if (file.isDirectory()) {
                // Add compiled classes directory
                typeSolver.add(new JavaParserTypeSolver(file.toPath()));
            } else if (path.endsWith(".jar")) {
                // Add dependency JARs
                typeSolver.add(new JarTypeSolver(path));
            }
        }

        symbolSolver = new JavaSymbolSolver(typeSolver);

        var configuration = new ParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        configuration.setSymbolResolver(symbolSolver);

        parser = new JavaParser(configuration);
    }

    public List<JavaClassModel> getClassesAnnotatedWith(String annotation) {
        return streamAllClasses()
                .filter(model -> model.isAnnotationPresent(annotation))
                .toList();
    }

    public Optional<JavaClassModel> getClass(String className) {
        return streamAllClasses()
                .filter(model -> model.getFullyQualifiedName().equals(className))
                .findAny();


    }

    private Stream<JavaClassModel> streamAllClasses() {
        return Stream.concat(
                        project.getCompileSourceRoots().stream(),
                        project.getTestCompileSourceRoots().stream()
                )
                .flatMap(this::streamClassDeclarations)
                .map(declaration -> {
                    try {
                        return symbolSolver.resolveDeclaration(declaration, ResolvedReferenceTypeDeclaration.class);
                    }catch (IllegalStateException e){
                        throw new IllegalStateException("Failed to resolve class declaration: " + declaration.getNameAsString(), e);
                    }
                })
                .map(MavenClassMapper::mapToClassModel)
                .filter(Objects::nonNull);
    }

    private Stream<ClassOrInterfaceDeclaration> streamClassDeclarations(String srcDir) {
        try {
            return Files.walk(Path.of(srcDir))
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> {
                        try {
                            return parser.parse(path);
                        } catch (IOException e) {
                            throw new IllegalArgumentException("Failed to parse " + path, e);
                        }
                    })
                    .flatMap(result -> result.getResult().stream())
                    .map(unit -> unit.findAll(ClassOrInterfaceDeclaration.class))
                    .flatMap(List::stream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to iterate source directory " + srcDir, e);
        }
    }
}
