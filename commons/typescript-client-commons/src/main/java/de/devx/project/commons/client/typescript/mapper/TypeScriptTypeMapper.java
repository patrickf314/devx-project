package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import de.devx.project.commons.client.typescript.io.TypeScriptTypeAlias;
import de.devx.project.commons.client.typescript.data.TypeScriptTypeModel;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mapper
public interface TypeScriptTypeMapper {

    @Named("mapType")
    default TypeScriptTypeModel mapType(ApiTypeModel type, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        if (type == null) {
            return null;
        }

        var typeAlias = typeAliases.get(type.getClassName());
        if (typeAlias != null && (typeAlias.getAnnotation() == null || type.getAnnotations().contains(typeAlias.getAnnotation()))) {
            return mapTypeAlias(typeAlias, !type.isRequired());
        }

        if (Pattern.class.getName().equals(type.getClassName())) {
            return createTypeScriptType("string", !type.isRequired());
        }

        return switch (type.getType()) {
            case DTO -> mapDTOType(type, typeAliases);
            case ENUM -> mapEnumType(type);
            case UNKNOWN -> mapUnknownType();
            case JAVA_TYPE -> mapJavaType(type, typeAliases);
            case GENERIC_TYPE -> mapGenericType(type);
        };
    }

    default TypeScriptTypeModel mapTypeAlias(TypeScriptTypeAlias typeAlias, boolean optional) {
        return createTypeScriptType(typeAlias.getTsType(), optional);
    }

    default TypeScriptTypeModel mapGenericType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), !model.isRequired());
    }

    default TypeScriptTypeModel mapJavaType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        return switch (model.getName()) {
            case "int", "double", "float", "long", "short", "byte", "number" ->
                    createTypeScriptType("number", !model.isRequired());
            case "boolean" -> createTypeScriptType("boolean", false);
            case "char", "string" -> createTypeScriptType("string", !model.isRequired());
            case "void" -> createTypeScriptType("void", false);
            case "array", "collection" -> mapArrayType(model, typeAliases);
            case "map" -> mapMapType(model, typeAliases);
            default -> mapUnknownType();
        };
    }

    default TypeScriptTypeModel mapArrayType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        return createTypeScriptType(mapType(model.getTypeArguments().get(0), typeAliases).getName() + "[]", !model.isRequired());
    }

    default TypeScriptTypeModel mapMapType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var keyType = mapType(model.getTypeArguments().get(0), typeAliases).getName();
        var valueType = mapType(model.getTypeArguments().get(1), typeAliases).getName();

        if (model.getTypeArguments().get(0).getType() == ApiTypeType.ENUM) {
            keyType = "string";
        }

        if (!keyType.equals("number") && !keyType.equals("string")) {
            throw new IllegalArgumentException("Invalid map type: key type must be string or number, but was " + keyType);
        }

        return createTypeScriptType("Record<" + keyType + ", " + valueType + " | undefined>", false);
    }

    default TypeScriptTypeModel mapUnknownType() {
        return createTypeScriptType("unknown", false);
    }

    default TypeScriptTypeModel mapEnumType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), !model.isRequired());
    }

    @Named("mapDTOType")
    default TypeScriptTypeModel mapDTOType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var builder = new StringBuilder();
        builder.append(model.getName());
        if (!model.getTypeArguments().isEmpty()) {
            builder.append("<")
                    .append(model.getTypeArguments().stream().map(t -> mapType(t, typeAliases))
                            .map(t -> t.getName() + (t.isOptional() ? " | undefined" : "")).collect(Collectors.joining(", ")))
                    .append(">");
        }

        return createTypeScriptType(builder.toString(), !model.isRequired());
    }

    private TypeScriptTypeModel createTypeScriptType(String name, boolean optional) {
        return new TypeScriptTypeModel(name, optional);
    }
}
