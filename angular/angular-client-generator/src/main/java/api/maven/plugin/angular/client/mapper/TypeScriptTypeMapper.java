package api.maven.plugin.angular.client.mapper;

import api.maven.plugin.angular.client.data.TypeScriptType;
import api.maven.plugin.angular.client.data.TypeScriptTypeAlias;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import org.mapstruct.Context;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TypeScriptTypeMapper {

    private TypeScriptTypeMapper() {
        // No instances
    }

    public static TypeScriptType mapType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        if (model == null) {
            return null;
        }

        var typeAlias = typeAliases.get(model.getClassName());
        if (typeAlias != null && (typeAlias.getAnnotation() == null || model.getAnnotations().contains(typeAlias.getAnnotation()))) {
            return mapTypeAlias(typeAlias, !model.isRequired());
        }

        if (Pattern.class.getName().equals(model.getClassName())) {
            return createTypeScriptType("string", !model.isRequired());
        }

        return switch (model.getType()) {
            case DTO -> mapDTOType(model, typeAliases);
            case ENUM -> mapEnumType(model);
            case UNKNOWN -> mapUnknownType();
            case JAVA_TYPE -> mapJavaType(model, typeAliases);
            case GENERIC_TYPE -> mapGenericType(model);
        };
    }

    private static TypeScriptType mapTypeAlias(TypeScriptTypeAlias typeAlias, boolean optional) {
        return createTypeScriptType(typeAlias.getTsType(), optional);
    }

    private static TypeScriptType mapGenericType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), !model.isRequired());
    }

    private static TypeScriptType mapJavaType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
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

    private static TypeScriptType mapArrayType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        return createTypeScriptType(mapType(model.getTypeArguments().get(0), typeAliases).getName() + "[]", !model.isRequired());
    }

    private static TypeScriptType mapMapType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
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

    private static TypeScriptType mapUnknownType() {
        return createTypeScriptType("unknown", false);
    }

    private static TypeScriptType mapEnumType(ApiTypeModel model) {
        return createTypeScriptType(model.getName(), !model.isRequired());
    }

    private static TypeScriptType mapDTOType(ApiTypeModel model, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
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

    private static TypeScriptType createTypeScriptType(String name, boolean optional) {
        var type = new TypeScriptType();
        type.setName(name);
        type.setOptional(optional);
        return type;
    }
}
