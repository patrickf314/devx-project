package de.devx.project.commons.generator.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.devx.project.commons.generator.model.JavaTypeModel.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link JavaTypeModel}
 */
class JavaTypeModelTest {

    @Test
    void testPrimitiveType() {
        var type = primitiveType("int", "Integer");

        assertThat(type.getQualifiedTypeName()).isEqualTo("int");
        assertThat(type.getNonPrimitiveQualifiedTypeName()).isEqualTo("Integer");
        assertThat(type.streamImports("some.test.package")).isEmpty();
    }

    @Test
    void testArrayType() {
        var type = arrayType(BOOLEAN);

        assertThat(type.getQualifiedTypeName()).isEqualTo("boolean[]");
        assertThat(type.getNonPrimitiveQualifiedTypeName()).isEqualTo("boolean[]");
        assertThat(type.streamImports("some.test.package")).isEmpty();
    }

    @Test
    void testObjectTypeWithoutGenerics() {
        var type = objectType("java.lang", "String");

        assertThat(type.getQualifiedTypeName()).isEqualTo("String");
        assertThat(type.getNonPrimitiveQualifiedTypeName()).isEqualTo("String");
        assertThat(type.streamImports("some.test.package")).isEmpty();
    }

    @Test
    void testObjectTypeWithGenerics() {
        var type = objectType("java.util", "List", List.of(
                objectType("java.lang", "String"))
        );

        assertThat(type.getQualifiedTypeName()).isEqualTo("List<String>");
        assertThat(type.getNonPrimitiveQualifiedTypeName()).isEqualTo("List<String>");
        assertThat(type.streamImports("some.test.package"))
                .hasSize(1)
                .first()
                .isEqualTo("java.util.List");
    }

    @Test
    void testObjectTypeWithPrimitiveGenerics() {
        var type = objectType("java.util", "List", List.of(BOOLEAN));

        assertThat(type.getQualifiedTypeName()).isEqualTo("List<Boolean>");
        assertThat(type.getNonPrimitiveQualifiedTypeName()).isEqualTo("List<Boolean>");
        assertThat(type.streamImports("some.test.package"))
                .hasSize(1)
                .first()
                .isEqualTo("java.util.List");
    }

    @Test
    void testGenericTemplateType() {
        var type = genericTemplateType("T");

        assertThat(type.getQualifiedTypeName()).isEqualTo("T");
        assertThat(type.getNonPrimitiveQualifiedTypeName()).isEqualTo("T");
        assertThat(type.streamImports("some.test.package")).isEmpty();
    }

    @Test
    void testNestedType() {
        var type = objectType("some.test.package", "Entity$NestedEntity");

        assertThat(type.getQualifiedTypeName()).isEqualTo("Entity.NestedEntity");
        assertThat(type.streamImports("some.other.package"))
                .hasSize(1)
                .first()
                .isEqualTo("some.test.package.Entity");
    }
}