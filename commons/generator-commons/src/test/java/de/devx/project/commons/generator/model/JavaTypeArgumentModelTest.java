package de.devx.project.commons.generator.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.devx.project.commons.generator.model.JavaTypeModel.genericTemplateType;
import static de.devx.project.commons.generator.model.JavaTypeModel.objectType;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link JavaTypeArgumentModel}
 */
class JavaTypeArgumentModelTest {

    @Test
    void testDefinitionOfSimpleTypeArgument() {
        var type = new JavaTypeArgumentModel("T");

        assertThat(type.getDefinition()).isEqualTo("T");
    }

    @Test
    void testDefinitionOfConstraintTypeArgument() {
        var type = new JavaTypeArgumentModel("T", objectType("java.lang", "Number"));

        assertThat(type.getDefinition()).isEqualTo("T extends Number");
    }

    @Test
    void testDefinitionOfNestedConstraintTypeArgument() {
        var type = new JavaTypeArgumentModel("E", objectType("java.lang", "Enum", List.of(genericTemplateType("E"))));

        assertThat(type.getDefinition()).isEqualTo("E extends Enum<E>");
    }

}