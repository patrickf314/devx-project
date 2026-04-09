package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiBrandedTypeModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.client.typescript.data.TypeScriptBrandedTypeModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface TypeScriptBrandedTypeMapper {

    @Mapping(target = "underlyingTsType", source = "underlyingType", qualifiedByName = "mapUnderlyingType")
    @Mapping(target = "underlyingZodSchema", source = "underlyingType", qualifiedByName = "mapUnderlyingZodSchema")
    TypeScriptBrandedTypeModel mapBrandedType(ApiBrandedTypeModel model);

    @Named("mapUnderlyingType")
    default String mapUnderlyingType(ApiTypeModel underlyingType) {
        return switch (underlyingType.getName()) {
            case "int", "double", "float", "long", "short", "byte", "number" -> "number";
            case "string", "char" -> "string";
            case "boolean" -> "boolean";
            default -> "unknown";
        };
    }

    @Named("mapUnderlyingZodSchema")
    default String mapUnderlyingZodSchema(ApiTypeModel underlyingType) {
        return switch (underlyingType.getName()) {
            case "int", "double", "float", "long", "short", "byte", "number" -> "z.number()";
            case "string", "char" -> "z.string()";
            case "boolean" -> "z.boolean()";
            default -> "z.unknown()";
        };
    }
}
