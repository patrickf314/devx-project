package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.client.typescript.data.TypeScriptDTOFieldModel;
import de.devx.project.commons.client.typescript.data.TypeScriptDTOModel;
import de.devx.project.commons.client.typescript.data.TypeScriptDependencyModel;
import de.devx.project.commons.client.typescript.io.TypeScriptTypeAlias;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper(uses = TypeScriptTypeMapper.class)
public interface TypeScriptDTOMapper {

    @Mapping(target = "extendedDTO", qualifiedByName = "mapDTOType")
    TypeScriptDTOModel mapDTO(ApiDTOModel model, Collection<TypeScriptDependencyModel> dependencies, @Context Map<String, TypeScriptTypeAlias> typeAliases);

    default List<TypeScriptDTOFieldModel> mapDTOFields(Map<String, ApiTypeModel> models, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        return models.entrySet()
                .stream()
                .map(entry -> mapDTOField(entry.getKey(), entry.getValue(), typeAliases))
                .toList();
    }

    @Mapping(target = "name", source = "name")
    @Mapping(target = "type", source = "type", qualifiedByName = "mapType")
    TypeScriptDTOFieldModel mapDTOField(String name, ApiTypeModel type, @Context Map<String, TypeScriptTypeAlias> typeAliases);
}
