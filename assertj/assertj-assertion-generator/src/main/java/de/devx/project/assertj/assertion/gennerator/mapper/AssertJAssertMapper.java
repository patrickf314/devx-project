package de.devx.project.assertj.assertion.gennerator.mapper;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertFieldModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.commons.generator.model.JavaClassFieldModel;
import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Mapper
public interface AssertJAssertMapper {

    List<JavaTypeModel> ABSTRACT_ASSERT_TYPE_ARGUMENTS = List.of(
            JavaTypeModel.genericTemplateType("SELF"),
            JavaTypeModel.genericTemplateType("ACTUAl")
    );

    JavaTypeModel ABSTRACT_ASSERT = JavaTypeModel.objectType("org.assertj.core.api", "AbstractAssert", ABSTRACT_ASSERT_TYPE_ARGUMENTS);

    @Mapping(target = "extendedAbstractAssertModel", source = "superClass", qualifiedByName = "mapSuperClass")
    AssertJAssertModel mapToAssert(JavaClassModel model);

    AssertJAssertFieldModel mapToAssertField(JavaClassFieldModel model);

    default List<AssertJAssertFieldModel> mapToAssertFields(List<JavaClassFieldModel> models) {
        return models.stream()
                .filter(not(JavaClassFieldModel::isStatic))
                .map(this::mapToAssertField)
                .toList();
    }

    @Named("mapSuperClass")
    default JavaTypeModel mapSuperClass(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<JavaTypeModel> superClassOptional) {
        if (superClassOptional.isEmpty()) {
            return ABSTRACT_ASSERT;
        }

        var superClass = superClassOptional.get();
        return mapToExtendedAbstractAssertModel(
                superClass.getPackageName().orElse(null),
                superClass.getName(),
                superClass.getTypeArguments().stream().map(JavaTypeModel::getName).toList()
        );
    }

    default JavaTypeModel mapToExtendedAbstractAssertModel(String packageName, String className, List<String> typeArguments) {
        if (packageName == null) {
            return ABSTRACT_ASSERT;
        }

        if ("java.lang".equals(packageName) && "Object".equals(className)) {
            return ABSTRACT_ASSERT;
        }

        var assertName = "Abstract" + className.replace('.', '$') + "Assert";
        return JavaTypeModel.objectType(packageName, assertName, Stream.concat(
                typeArguments.stream().map(JavaTypeModel::genericTemplateType),
                ABSTRACT_ASSERT_TYPE_ARGUMENTS.stream()
        ).toList());
    }
}
