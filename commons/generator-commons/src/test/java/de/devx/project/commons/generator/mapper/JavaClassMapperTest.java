package de.devx.project.commons.generator.mapper;

import de.devx.project.commons.generator.type.JavaAccessModifierType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link JavaClassMapper}
 */
class JavaClassMapperTest {

    @Getter
    @AllArgsConstructor
    private static class SimpleTestDTO {
        private String name;

        public static SimpleTestDTO create(String name) {
            return new SimpleTestDTO(name);
        }
    }

    @Test
    void testMapSimpleClass() {
        var model = JavaClassMapper.mapToClassModel(SimpleTestDTO.class);

        assertThat(model.getPackageName()).isEqualTo("de.devx.project.commons.generator.mapper");
        assertThat(model.getName()).isEqualTo("JavaClassMapperTest.SimpleTestDTO");
        assertThat(model.getDeclaration()).isEqualTo("JavaClassMapperTest.SimpleTestDTO");
        assertThat(model.getFields())
                .hasSize(1)
                .first()
                .satisfies(field -> {
                    assertThat(field.getName()).isEqualTo("name");
                    assertThat(field.getType().getQualifiedTypeName()).isEqualTo("String");
                    assertThat(field.getAccessModifiers()).containsOnly(JavaAccessModifierType.PRIVATE);
                });
        assertThat(model.getMethods())
                .satisfiesOnlyOnce(method -> {
                    assertThat(method.getName()).isEqualTo("getName");
                    assertThat(method.getReturnType().getQualifiedTypeName()).isEqualTo("String");
                    assertThat(method.getAccessModifiers()).containsOnly(JavaAccessModifierType.PUBLIC);
                    assertThat(method.getParameters()).isEmpty();
                })
                .satisfiesOnlyOnce(method -> {
                    assertThat(method.getName()).isEqualTo("create");
                    assertThat(method.getReturnType().getQualifiedTypeName()).isEqualTo("JavaClassMapperTest.SimpleTestDTO");
                    assertThat(method.getAccessModifiers()).containsOnly(JavaAccessModifierType.PUBLIC, JavaAccessModifierType.STATIC);
                    assertThat(method.getParameters())
                            .hasSize(1)
                            .first()
                            .satisfies(parameter -> {
                                assertThat(parameter.getType().getQualifiedTypeName()).isEqualTo("String");
                            });
                });
    }

    @Test
    void testMapClassWithTypeArguments() {
        var model = JavaClassMapper.mapToClassModel(List.class);

        assertThat(model.getPackageName()).isEqualTo("java.util");
        assertThat(model.getName()).isEqualTo("List");
        assertThat(model.getTypeArguments())
                .hasSize(1)
                .first()
                .satisfies(typeArgument -> {
                    assertThat(typeArgument.getName()).isEqualTo("E");
                    assertThat(typeArgument.getTypeConstraint()).isEmpty();
                });
    }

    @Test
    void testMapClassWithConstrainTypeArguments() {
        var model = JavaClassMapper.mapToClassModel(Enum.class);

        assertThat(model.getPackageName()).isEqualTo("java.lang");
        assertThat(model.getName()).isEqualTo("Enum");
        assertThat(model.getTypeArguments())
                .hasSize(1)
                .first()
                .satisfies(typeArgument -> {
                    assertThat(typeArgument.getName()).isEqualTo("E");
                    assertThat(typeArgument.getTypeConstraint())
                            .isPresent()
                            .get()
                            .satisfies(constraint -> {
                                assertThat(constraint.getQualifiedTypeName()).isEqualTo("Enum<E>");
                            });
                });
    }
}