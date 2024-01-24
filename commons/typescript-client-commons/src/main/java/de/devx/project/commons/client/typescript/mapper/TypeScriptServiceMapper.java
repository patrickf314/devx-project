package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiServiceEndpointModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodParameterModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceModel;
import de.devx.project.commons.client.typescript.properties.TypeScriptTypeAlias;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(uses = {TypeScriptTypeMapper.class, TypeScriptServiceMethodMapper.class, TypeScriptPathMapper.class})
public interface TypeScriptServiceMapper {

    TypeScriptServiceModel mapService(ApiServiceEndpointModel endpointModel, @Context Map<String, TypeScriptTypeAlias> typeAliases);

    @AfterMapping
    default void adjustParameterNames(@MappingTarget TypeScriptServiceModel service) {
        var basePathParams = service.getBasePaths().get(0).getParams();

        for (var method : service.getMethods()) {
            var map = method.getPathParams()
                    .stream()
                    .collect(Collectors.toMap(TypeScriptServiceMethodParameterModel::getParameterName, TypeScriptServiceMethodParameterModel::getName));

            method.setBasePathParamNames(basePathParams.stream().map(map::get).toList());

            var path = method.getPath();
            for (var entry : map.entrySet()) {
                if (!Objects.equals(entry.getKey(), entry.getValue())) {
                    path = path.replaceParamName(entry.getKey(), entry.getValue());
                }
            }

            method.setPath(path);
        }
    }
}
