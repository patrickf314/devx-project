package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import de.devx.project.commons.client.typescript.data.TypeScriptTypeModel;
import de.devx.project.commons.client.typescript.properties.TypeScriptTypeAlias;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

@Mapper
public interface TypeScriptTypeMapper {

    @Named("mapType")
    default TypeScriptTypeModel mapType(ApiTypeModel type, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        if (type == null) {
            return null;
        }

        var typeAlias = typeAliases.get(type.getClassName());
        if (typeAlias != null) {
            return mapTypeAlias(typeAlias, !type.isRequired());
        }

        if (Pattern.class.getName().equals(type.getClassName())) {
            return createTypeScriptType("string", !type.isRequired(), emptySet());
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
        return createTypeScriptType(typeAlias.getType(), optional, Set.of(typeAlias.getClassName()));
    }

    default TypeScriptTypeModel mapGenericType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), !model.isRequired(), emptySet());
    }

    default TypeScriptTypeModel mapJavaType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        return switch (model.getName()) {
            case "int", "double", "float", "long", "short", "byte", "number" ->
                    createTypeScriptType("number", !model.isRequired(), emptySet());
            case "boolean" -> createTypeScriptType("boolean", false, emptySet());
            case "char", "string" -> createTypeScriptType("string", !model.isRequired(), emptySet());
            case "void" -> createTypeScriptType("void", false, emptySet());
            case "array", "collection" -> mapArrayType(model, typeAliases);
            case "map" -> mapMapType(model, typeAliases);
            default -> mapUnknownType();
        };
    }

    default TypeScriptTypeModel mapArrayType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var typeArgument = mapType(model.getTypeArguments().get(0), typeAliases);
        return createTypeScriptType(typeArgument.getName() + "[]", !model.isRequired(), typeArgument.getDependentClassNames());
    }

    default TypeScriptTypeModel mapMapType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var keyType = mapType(model.getTypeArguments().get(0), typeAliases).getName();
        var valueType = mapType(model.getTypeArguments().get(1), typeAliases);

        if (model.getTypeArguments().get(0).getType() == ApiTypeType.ENUM) {
            keyType = "string";
        }

        if (!keyType.equals("number") && !keyType.equals("string")) {
            throw new IllegalArgumentException("Invalid map type: key type must be string or number, but was " + keyType);
        }

        return createTypeScriptType("Record<" + keyType + ", " + valueType.getName() + " | undefined>", false, valueType.getDependentClassNames());
    }

    default TypeScriptTypeModel mapUnknownType() {
        return createTypeScriptType("unknown", false, emptySet());
    }

    default TypeScriptTypeModel mapEnumType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), !model.isRequired(), Set.of(model.getClassName()));
    }

    @Named("mapDTOType")
    default TypeScriptTypeModel mapDTOType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        if(model == null) {
            return null;
        }

        var typeArguments = model.getTypeArguments()
                .stream()
                .map(t -> mapType(t, typeAliases))
                .toList();

        var builder = new StringBuilder();
        builder.append(model.getName());
        if (!model.getTypeArguments().isEmpty()) {
            builder.append("<")
                    .append(typeArguments.stream()
                            .map(t -> t.getName() + (t.isOptional() ? " | undefined" : ""))
                            .collect(Collectors.joining(", "))
                    )
                    .append(">");
        }

        var dependentClassNames = Stream.concat(
                typeArguments.stream().map(TypeScriptTypeModel::getDependentClassNames).flatMap(Set::stream),
                Stream.of(model.getClassName())
        ).collect(Collectors.toSet());

        return createTypeScriptType(builder.toString(), !model.isRequired(), dependentClassNames);
    }

    private TypeScriptTypeModel createTypeScriptType(String name, boolean optional, Set<String> dependentClassNames) {
        return new TypeScriptTypeModel(name, optional, dependentClassNames);
    }
}
