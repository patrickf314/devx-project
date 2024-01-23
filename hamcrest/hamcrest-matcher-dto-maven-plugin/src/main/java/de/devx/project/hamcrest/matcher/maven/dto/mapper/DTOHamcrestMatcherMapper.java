package de.devx.project.hamcrest.matcher.maven.dto.mapper;

import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestMatcherModel;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.*;

import static de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel.*;

@Mapper
public interface DTOHamcrestMatcherMapper {

    String JAVA_LANG_PACKAGE = "java.lang";

    default List<HamcrestMatcherModel> mapApiDTOs(ApiModel model) {
        return mapApiDTOs(model.getDtos().values(), model);
    }

    List<HamcrestMatcherModel> mapApiDTOs(Collection<ApiDTOModel> dtos, @Context ApiModel model);

    @Mapping(target = "packageName", source = "className", qualifiedByName = "packageName")
    @Mapping(target = "className", source = "name")
    @Mapping(target = "generics", source = "typeArguments")
    @Mapping(target = "fields", source = ".")
    HamcrestMatcherModel mapApiDTO(ApiDTOModel dto, @Context ApiModel model);

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

    default List<HamcrestClassFieldModel> mapFields(ApiDTOModel dto, @Context ApiModel model) {
        var list = new ArrayList<HamcrestClassFieldModel>();
        if (dto.getExtendedDTO() != null) {
            list.addAll(mapFields(model.getDtos().get(dto.getExtendedDTO().getClassName()), model));
        }

        list.addAll(dto.getFields()
                .entrySet()
                .stream()
                .map(fieldModel -> mapField(fieldModel, dto.isJavaRecord()))
                .toList());

        return list;
    }

    default HamcrestClassFieldModel mapField(Map.Entry<String, ApiTypeModel> fieldModel, @Context boolean javaRecord) {
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

        return new HamcrestClassFieldModel(mapType(type), name, getter);
    }

    default HamcrestClassFieldTypeModel mapType(ApiTypeModel type) {
        if (type.getType() == ApiTypeType.GENERIC_TYPE) {
            return genericType(type.getName());
        }

        if (type.getType() == ApiTypeType.UNKNOWN) {
            return objectType(JAVA_LANG_PACKAGE, "Object", List.of());
        }

        var generics = type.getTypeArguments()
                .stream()
                .map(this::mapType)
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
            return objectType(JAVA_LANG_PACKAGE, "String", Collections.emptyList());
        }

        if ("number".equals(type.getName())) {
            var i = type.getClassName().lastIndexOf('.');
            return objectType(JAVA_LANG_PACKAGE, type.getClassName().substring(i + 1), Collections.emptyList());
        }

        if ("int".equals(type.getName())) {
            return type.isRequired() ? primaryType("int", "Integer") : objectType(JAVA_LANG_PACKAGE, "Integer", Collections.emptyList());
        }

        return type.isRequired() ? primaryType(type.getName(), upperFirstChar(type.getName())) : objectType(JAVA_LANG_PACKAGE, upperFirstChar(type.getName()), Collections.emptyList());
    }

    private String upperFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
