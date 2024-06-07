package de.devx.project.commons.client.typescript.io;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class TypeScriptFileGeneratorTest {

    @TempDir
    private static File outputDirectory;

    private final TypeScriptFileGenerator generator = new TypeScriptFileGenerator(outputDirectory.getAbsolutePath());

    static Stream<Arguments> testFileNames() {
        return Stream.of(
                Arguments.of("TestServiceAPI", "test.service.ts"),
                Arguments.of("SecondTestServiceApi", "second-test.service.ts"),
                Arguments.of("TestDTO", "test.dto.ts"),
                Arguments.of("SecondTestDto", "second-test.dto.ts"),
                Arguments.of("TestType", "test.type.ts"),
                Arguments.of("SecondTestType", "second-test.type.ts"),
                Arguments.of("CustomData", "custom-data.ts")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFileNames(String className, String expectedFileName) {
        var actualFileName = generator.fileName(className);

        assertThat(actualFileName, is(expectedFileName));
    }

    static Stream<Arguments> testImportPaths() {
        return Stream.of(
                Arguments.of("de.devx.project.test.service1", "de.devx.project.test.service1", "."),
                Arguments.of("de.devx.project.test.service1.dto", "de.devx.project.test.service1", ".."),
                Arguments.of("de.devx.project.test.service1", "de.devx.project.test.service2", "../service2"),
                Arguments.of("de.devx.project.test.service1", "de.devx.project.test.service1.dto", "./dto"),
                Arguments.of("de.devx.project.test.service1.dto", "de.devx.project.test.service1.type", "../type"),
                Arguments.of("de.devx.project.test.service1.dto", "de.devx.project.test.service2.dto", "../../service2/dto"),
                Arguments.of("", "nested.package", "./nested/package"),
                Arguments.of("nested.package", "", "../.."),
                Arguments.of("", "", ".")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testImportPaths(String currentPackage, String targetPackage, String expectedImportPath) {
        var actualImportPath = generator.importPath(currentPackage, targetPackage);

        assertThat(actualImportPath, is(expectedImportPath));
    }

}