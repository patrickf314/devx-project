package de.devx.project.assertj.condition.maven.mapper;

import de.devx.project.assertj.condition.generator.data.AssertjClassFieldModel;
import de.devx.project.assertj.condition.generator.data.AssertjClassFieldTypeModel;
import de.devx.project.assertj.condition.generator.data.AssertjConditionModel;
import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.*;

import static de.devx.project.assertj.condition.generator.data.AssertjClassFieldTypeModel.*;
import static java.util.Collections.emptyList;

@Mapper
public interface AssertjConditionMapper {

    String JAVA_LANG_PACKAGE = "java.lang";

    default List<AssertjConditionModel> mapApiDTOs(ApiModel model) {
        return mapApiDTOs(model.getDtos().values(), model);
    }

    List<AssertjConditionModel> mapApiDTOs(Collection<ApiDTOModel> dtos, @Context ApiModel model);

    @Mapping(target = "packageName", source = "className", qualifiedByName = "packageName")
    @Mapping(target = "className", source = "name")
    @Mapping(target = "generics", source = "typeArguments")
    @Mapping(target = "fields", source = ".")
    @Mapping(target = "enclosingDTO", source = "enclosingDTO.name")
    AssertjConditionModel mapApiDTO(ApiDTOModel dto, @Context ApiModel model);

    @Named("packageName")
    default String packageName(String className) {
        var i = className.lastIndexOf('.');
        if (i == -1) {
            throw new IllegalArgumentException("Failed to extract package name from class name " + className);
        }
        return className.substring(0, i);
    }

    default String generics(List<String> typeArguments) {
        return typeArguments.isEmpty() ? "" : "<" + String.join(", ", typeArguments) + ">";
    }

    default List<AssertjClassFieldModel> mapFields(ApiDTOModel dto, @Context ApiModel model) {
        return mapFields(dto, model, dto.getTypeArguments().stream().map(AssertjClassFieldTypeModel::genericType).toList());
    }

    default List<AssertjClassFieldModel> mapFields(ApiDTOModel dto, @Context ApiModel model, @Context List<AssertjClassFieldTypeModel> typeArguments) {
        var typeArgumentsAliases = mapTypeArguments(dto, typeArguments);

        var list = new ArrayList<AssertjClassFieldModel>();
        if (dto.getExtendedDTO() != null) {
            var extendedDTO = model.getDtos().get(dto.getExtendedDTO().getClassName());
            list.addAll(mapFields(extendedDTO, model, dto.getExtendedDTO()
                    .getTypeArguments()
                    .stream()
                    .map(type -> mapType(type, typeArgumentsAliases))
                    .toList()
            ));
        }

        var typeArgumentAliases = mapTypeArguments(dto, typeArguments);

        list.addAll(dto.getFields()
                .entrySet()
                .stream()
                .map(fieldModel -> mapField(fieldModel, dto.isJavaRecord(), typeArgumentAliases))
                .toList());

        return list;
    }

    private HashMap<String, AssertjClassFieldTypeModel> mapTypeArguments(ApiDTOModel dto, List<AssertjClassFieldTypeModel> typeArguments) {
        if (typeArguments.size() != dto.getTypeArguments().size()) {
            throw new IllegalArgumentException("Invalid type arguments");
        }

        var typeArgumentAliases = new HashMap<String, AssertjClassFieldTypeModel>();
        for (var i = 0; i < typeArguments.size(); i++) {
            typeArgumentAliases.put(dto.getTypeArguments().get(i), typeArguments.get(i));
        }
        return typeArgumentAliases;
    }

    default AssertjClassFieldModel mapField(Map.Entry<String, ApiTypeModel> fieldModel, @Context boolean javaRecord, @Context Map<String, AssertjClassFieldTypeModel> typeArguments) {
        var name = fieldModel.getKey();
        var type = fieldModel.getValue();
        String getter;

        if (javaRecord) {
            getter = name;
        } else if (type.getClassName() == null && "boolean".equals(type.getName())) {
            getter = "is" + upperFirstChar(name);
        } else {
            getter = "get" + upperFirstChar(name);
        }

        return new AssertjClassFieldModel(mapType(type, typeArguments), name, getter);
    }

    default AssertjClassFieldTypeModel mapType(ApiTypeModel type, @Context Map<String, AssertjClassFieldTypeModel> typeArguments) {
        if (type.getType() == ApiTypeType.GENERIC_TYPE) {
            return Objects.requireNonNull(typeArguments.get(type.getName()));
        }

        if (type.getType() == ApiTypeType.UNKNOWN) {
            return objectType(JAVA_LANG_PACKAGE, "Object", List.of());
        }

        var generics = type.getTypeArguments()
                .stream()
                .map(typeArgument -> mapType(typeArgument, typeArguments))
                .toList();

        if (type.getType() == ApiTypeType.DTO || type.getType() == ApiTypeType.ENUM) {
            return objectType(packageName(type.getClassName()), type.getName(), generics);
        }

        if ("collection".equals(type.getName())) {
            return objectType("java.util", "Collection", generics);
        }

        if ("map".equals(type.getName())) {
            return objectType("java.util", "Map", generics);
        }

        if ("array".equals(type.getName())) {
            return arrayType(generics.get(0));
        }

        if ("string".equals(type.getName())) {
            return objectType(JAVA_LANG_PACKAGE, "String", emptyList());
        }

        if ("number".equals(type.getName())) {
            var i = type.getClassName().lastIndexOf('.');
            return objectType(JAVA_LANG_PACKAGE, type.getClassName().substring(i + 1), emptyList());
        }

        if ("int".equals(type.getName())) {
            return type.isRequired() ? primaryType("int", "Integer") : objectType(JAVA_LANG_PACKAGE, "Integer", emptyList());
        }

        return type.isRequired() ? primaryType(type.getName(), upperFirstChar(type.getName())) : objectType(JAVA_LANG_PACKAGE, upperFirstChar(type.getName()), emptyList());
    }

    private String upperFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
