package de.devx.project.freemarker.generator.parser;

import de.devx.project.freemarker.generator.data.FtlTemplateModel;
import de.devx.project.freemarker.generator.data.FtlVariableModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FtlTemplateParserTest {

    private final FtlTemplateParser sut = new FtlTemplateParser();

    @Test
    void testNonExistentDirectoryReturnsEmptyList() throws IOException {
        var result = sut.parseTemplates(Path.of("/does/not/exist"), "com.example");

        assertThat(result).isEmpty();
    }

    @Test
    void testEmptyDirectoryReturnsEmptyList(@TempDir Path tempDir) throws IOException {
        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result).isEmpty();
    }

    @Test
    void testIgnoresNonFtlFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("readme.txt"), "not a template");

        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result).isEmpty();
    }

    @Test
    void testTemplateWithNoVariablesHasEmptyVariablesList(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Plain.ftl"), "Hello world");

        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getVariables()).isEmpty();
    }

    @Test
    void testParsesJavaLangTypeWithoutImport(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("WelcomeEmail.ftl"), """
                <#-- @ftlvariable name="subject" type="java.lang.String" -->
                Hello ${subject}
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result).hasSize(1);
        var template = result.getFirst();
        assertThat(template.getTemplateName()).isEqualTo("WelcomeEmail");
        assertThat(template.getClassName()).isEqualTo("WelcomeEmail");
        assertThat(template.getModelClassName()).isEqualTo("WelcomeEmailModel");
        assertThat(template.getTemplateIdentifier()).isEqualTo("WELCOME_EMAIL");
        assertThat(template.getPackageName()).isEqualTo("com.example");
        assertThat(template.getTemplatePath()).isEqualTo("WelcomeEmail.ftl");
        assertThat(template.getVariables())
                .singleElement()
                .satisfies(v -> {
                    assertThat(v.getName()).isEqualTo("subject");
                    assertThat(v.getSimpleType()).isEqualTo("String");
                    assertThat(v.getImports()).isEmpty();
                });
    }

    @Test
    void testParsesPrimitiveType(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Counter.ftl"), """
                <#-- @ftlvariable name="count" type="int" -->
                ${count}
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        var variable = result.getFirst().getVariables().getFirst();
        assertThat(variable.getSimpleType()).isEqualTo("int");
        assertThat(variable.getImports()).isEmpty();
    }

    @Test
    void testParsesCustomTypeWithImport(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Report.ftl"), """
                <#-- @ftlvariable name="data" type="com.example.ReportData" -->
                """);

        var result = sut.parseTemplates(tempDir, "com.example.templates");

        var variable = result.getFirst().getVariables().getFirst();
        assertThat(variable.getSimpleType()).isEqualTo("ReportData");
        assertThat(variable.getImports()).containsExactly("com.example.ReportData");
    }

    @Test
    void testParsesGenericTypeWithImport(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("ItemList.ftl"), """
                <#-- @ftlvariable name="items" type="java.util.List<java.lang.String>" -->
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        var variable = result.getFirst().getVariables().getFirst();
        assertThat(variable.getSimpleType()).isEqualTo("List<String>");
        assertThat(variable.getImports()).containsExactly("java.util.List");
    }

    @Test
    void testParsesNestedGenericType(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Index.ftl"), """
                <#-- @ftlvariable name="index" type="java.util.Map<java.lang.String,java.util.List<java.lang.Integer>>" -->
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        var variable = result.getFirst().getVariables().getFirst();
        assertThat(variable.getSimpleType()).isEqualTo("Map<String, List<Integer>>");
        assertThat(variable.getImports()).containsExactlyInAnyOrder("java.util.Map", "java.util.List");
    }

    @Test
    void testParsesMultipleVariables(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Invoice.ftl"), """
                <#-- @ftlvariable name="recipient" type="java.lang.String" -->
                <#-- @ftlvariable name="items" type="java.util.List<com.example.LineItem>" -->
                <#-- @ftlvariable name="total" type="java.math.BigDecimal" -->
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result.getFirst().getVariables())
                .extracting(FtlVariableModel::getName)
                .containsExactly("recipient", "items", "total");
    }

    @Test
    void testDeduplicatesDuplicateVariableName(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Dup.ftl"), """
                <#-- @ftlvariable name="subject" type="java.lang.String" -->
                <#-- @ftlvariable name="subject" type="java.lang.Integer" -->
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result.getFirst().getVariables())
                .singleElement()
                .extracting(FtlVariableModel::getName)
                .isEqualTo("subject");
    }

    @Test
    void testRecursivelyFindsTemplatesInSubdirectories(@TempDir Path tempDir) throws IOException {
        var subDir = tempDir.resolve("sub");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("Nested.ftl"), """
                <#-- @ftlvariable name="value" type="java.lang.String" -->
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTemplateName()).isEqualTo("Nested");
        assertThat(result.getFirst().getTemplatePath()).isEqualTo("sub/Nested.ftl");
    }

    @ParameterizedTest
    @CsvSource({
            "MyTemplate.ftl,          MyTemplate",
            "my-template.ftl,         MyTemplate",
            "my_template.ftl,         MyTemplate",
            "welcome-email.ftl,       WelcomeEmail",
            "dto-template.ts.ftl,     DtoTemplate",
    })
    void testClassNameConversion(String fileName, String expectedClassName, @TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve(fileName), "");

        var result = sut.parseTemplates(tempDir, "com.example");

        assertThat(result).singleElement()
                .extracting(FtlTemplateModel::getClassName)
                .isEqualTo(expectedClassName);
    }

    @Test
    void testArrayTypeHasNoImportForJavaLangComponent(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("Arr.ftl"), """
                <#-- @ftlvariable name="tags" type="java.lang.String[]" -->
                """);

        var result = sut.parseTemplates(tempDir, "com.example");

        var variable = result.getFirst().getVariables().getFirst();
        assertThat(variable.getSimpleType()).isEqualTo("String[]");
        assertThat(variable.getImports()).isEmpty();
    }
}
