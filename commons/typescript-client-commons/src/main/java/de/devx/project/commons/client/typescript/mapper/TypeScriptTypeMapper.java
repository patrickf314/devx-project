package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import de.devx.project.commons.client.typescript.data.TypeScriptTypeModel;
import de.devx.project.commons.client.typescript.properties.TypeScriptTypeAlias;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            return createTypeScriptType("string", null, !type.isRequired(), emptySet(), "z.string()");
        }

        return switch (type.getType()) {
            case DTO -> mapDTOType(type, typeAliases);
            case ENUM -> mapEnumType(type);
            case UNKNOWN -> mapUnknownType();
            case JAVA_TYPE -> mapJavaType(type, typeAliases);
            case GENERIC_TYPE -> mapGenericType(type);
            case BRANDED_TYPE -> mapBrandedType(type);
        };
    }

    default TypeScriptTypeModel mapTypeAlias(TypeScriptTypeAlias typeAlias, boolean optional) {
        return createTypeScriptType(typeAlias.getType(), typeAlias.getClassName(), optional, emptySet(), typeAlias.getZodSchema());
    }

    default TypeScriptTypeModel mapGenericType(ApiTypeModel model) {
        return new TypeScriptTypeModel(model.getName(), null, !model.isRequired(), emptySet(), model.getName());
    }

    default TypeScriptTypeModel mapJavaType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        return switch (model.getName()) {
            case "int", "double", "float", "long", "short", "byte", "number" ->
                    createTypeScriptType("number", null, !model.isRequired(), emptySet(), "z.number()");
            case "boolean" -> createTypeScriptType("boolean", null, false, emptySet(), "z.boolean()");
            case "char", "string" ->
                    createTypeScriptType("string", null, !model.isRequired(), emptySet(), "z.string()");
            case "void" -> createTypeScriptType("void", null, false, emptySet(), "z.void()");
            case "array", "collection" -> mapArrayType(model, typeAliases);
            case "map" -> mapMapType(model, typeAliases);
            case "object" -> createTypeScriptType("unknown", null, !model.isRequired(), emptySet(), "z.unknown()");
            default -> mapUnknownType();
        };
    }

    default TypeScriptTypeModel mapArrayType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var typeArgument = mapType(model.getTypeArguments().getFirst(), typeAliases);
        return createTypeScriptType(typeArgument.getName() + "[]", null, !model.isRequired(), Set.of(typeArgument), "z.array(" + typeArgument.getZodSchema() + ")");
    }

    default TypeScriptTypeModel mapMapType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var keyType = mapType(model.getTypeArguments().get(0), typeAliases);
        var valueType = mapType(model.getTypeArguments().get(1), typeAliases);
        var isEnum = model.getTypeArguments().get(0).getType() == ApiTypeType.ENUM;

        if (!keyType.getName().equals("number") && !keyType.getName().equals("string") && !isEnum) {
            throw new IllegalArgumentException("Invalid map type: key type must be string, number or an enum, but was " + keyType);
        }

        var dependentTypes = keyType.equals(valueType) ? Set.of(keyType) : Set.of(keyType, valueType);
        return createTypeScriptType("Record<" + keyType.getName() + ", " + valueType.getName() + " | undefined>", null, false, dependentTypes, "z.record(" + keyType.getZodSchema() + ", " + valueType.getZodSchema() + ".optional())");
    }

    default TypeScriptTypeModel mapUnknownType() {
        return createTypeScriptType("unknown", null, false, emptySet(), "z.unknown()");
    }

    default TypeScriptTypeModel mapEnumType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), model.getClassName(), !model.isRequired(), emptySet(), model.getName() + "Schema");
    }

    default TypeScriptTypeModel mapBrandedType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), model.getClassName(), !model.isRequired(), emptySet(), model.getName() + "Schema");
    }

    @Named("mapDTOType")
    default TypeScriptTypeModel mapDTOType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        if (model == null) {
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

        var zodSchema = mapToZodSchema(model);
        return createTypeScriptType(builder.toString(), model.getClassName(), !model.isRequired(), new HashSet<>(typeArguments), zodSchema);
    }

    private static String mapToZodSchema(ApiTypeModel model) {
        if (model.getTypeArguments().isEmpty()) {
            return model.getName() + "Schema";
        }

        var genericArguments = model.getTypeArguments().stream().map(TypeScriptTypeMapper::mapToZodSchema).collect(Collectors.joining(", "));
        return "create" + model.getName() + "Schema(" + genericArguments + ")";
    }

    private TypeScriptTypeModel createTypeScriptType(String name, String className, boolean optional, Set<TypeScriptTypeModel> dependentTypes, String zodSchema) {
        return new TypeScriptTypeModel(name, className, optional, dependentTypes, zodSchema);
    }
}
