package de.devx.project.spring.web.processor;

import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.io.ApiModelReader;
import de.devx.project.commons.api.model.type.ApiTypeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class SpringApiModelGeneratorTest {

    private static final String SOURCE_RESOURCE_BASE = "/de/devx/project/test/sources/";

    @TempDir
    Path outputDir;

    @Test
    void testBrandedTypeIsRegistered() throws Exception {
        var model = compile("UserID.java", "UserDTO.java", "UserController.java");

        assertThat(model.getBrandedTypes().containsKey("de.devx.project.test.UserID"), is(true));

        var userID = model.getBrandedTypes().get("de.devx.project.test.UserID");
        assertThat(userID.getName(), is("UserID"));
        assertThat(userID.getUnderlyingType().getType(), is(ApiTypeType.JAVA_TYPE));
        assertThat(userID.getUnderlyingType().getName(), is("int"));
    }

    @Test
    void testDTOFieldHasBrandedType() throws Exception {
        var model = compile("UserID.java", "UserDTO.java", "UserController.java");

        var dto = model.getDtos().get("de.devx.project.test.UserDTO");
        var idField = dto.getFields().get("id");

        assertThat(idField.getType(), is(ApiTypeType.BRANDED_TYPE));
        assertThat(idField.getName(), is("UserID"));
        assertThat(idField.getClassName(), is("de.devx.project.test.UserID"));
    }

    private ApiModel compile(String... sourceFiles) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);

        var sources = resolveSourceFiles(sourceFiles);
        var compilationUnits = fileManager.getJavaFileObjects(sources);
        var options = List.of(
                "-classpath", System.getProperty("java.class.path"),
                "-d", outputDir.toString()
        );

        var task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);
        task.setProcessors(List.of(new SpringAnnotationProcessor()));
        assertThat("Compilation failed", task.call(), is(true));

        try (var reader = new ApiModelReader(outputDir.resolve("api-model.json").toFile())) {
            return reader.read();
        }
    }

    private File[] resolveSourceFiles(String[] fileNames) {
        var files = new File[fileNames.length];
        for (var i = 0; i < fileNames.length; i++) {
            files[i] = resolveSourceFile(fileNames[i]);
        }
        return files;
    }

    private File resolveSourceFile(String fileName) {
        try {
            var resource = getClass().getResource(SOURCE_RESOURCE_BASE + fileName);
            if (resource == null) {
                throw new IllegalStateException("Test resource not found: " + SOURCE_RESOURCE_BASE + fileName);
            }
            return Path.of(resource.toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to resolve test resource: " + fileName, e);
        }
    }
}
