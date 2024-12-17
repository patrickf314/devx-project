package de.devx.project.commons.generator.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link JavaClassModel}
 */
class JavaClassModelTest {

    @Test
    void testGetDeclarationOfSimpleClass() {
        var model = new JavaClassModel("de.devx.project.commons.generator.model", "TestDTO", emptyList(), emptyList());

        assertThat(model.getDeclaration()).isEqualTo("TestDTO");
    }

    @Test
    void testGetDeclarationOfClassWithTypeArgument() {
        var model = new JavaClassModel("java.util", "List", List.of(new JavaTypeArgumentModel("E")), emptyList(), emptyList());

        assertThat(model.getDeclaration()).isEqualTo("List<E>");
    }

    @Test
    void testGetDeclarationOfClassWithTwoTypeArgument() {
        var model = new JavaClassModel("java.util", "Map", List.of(
                new JavaTypeArgumentModel("K"),
                new JavaTypeArgumentModel("V")
        ), emptyList(), emptyList());

        assertThat(model.getDeclaration()).isEqualTo("Map<K, V>");
    }
}