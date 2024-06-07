package api.maven.plugin.angular.client.mapper;

import api.maven.plugin.angular.client.data.*;
import api.maven.plugin.angular.client.utils.TypeScriptMethodNameUtils;
import de.devx.project.commons.api.model.data.*;
import de.devx.project.commons.api.model.type.ApiMethodParameterType;
import de.devx.project.commons.api.model.type.ApiMethodResponseType;
import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(uses = {TypeScriptTypeMapper.class, TypeScriptPathMapper.class})
public interface TypeScriptMapper {

    TypeScriptEnum mapEnum(ApiEnumModel model);

    TypeScriptDTO mapDTO(ApiDTOModel dtoModel, Collection<TypeScriptDependency> dependencies, @Context Map<String, TypeScriptTypeAlias> typeAliases);

    TypeScriptService mapService(ApiServiceEndpointModel endpointModel, Collection<TypeScriptDependency> dependencies, @Context Map<String, TypeScriptTypeAlias> typeAliases);

    default List<TypeScriptServiceMethod> mapMethods(Map<String, List<ApiMethodModel>> methodModels, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var methods = new ArrayList<TypeScriptServiceMethod>();
        for (var models : methodModels.values()) {
            var nameTable = TypeScriptMethodNameUtils.createNameTable(models);
            for (var methodModel : models) {
                for (var httpMethod : methodModel.getHttpMethods()) {
                    var paths = methodModel.getPaths();
                    for (var path : paths) {
                        var method = mapMethod(methodModel, httpMethod, path, typeAliases);
                        method.setName(nameTable.lookupNameFor(methodModel.getName(), httpMethod, path));
                        methods.add(method);
                    }

                    if (paths.isEmpty()) {
                        var method = mapMethod(methodModel, httpMethod, "", typeAliases);
                        method.setName(nameTable.lookupNameFor(methodModel.getName(), httpMethod, ""));
                        methods.add(method);
                    }
                }
            }
        }
        return methods;
    }

    @Mapping(target = "returnTypeWrapper", ignore = true)
    @Mapping(target = "formData", ignore = true)
    @Mapping(target = "headerParams", ignore = true)
    @Mapping(target = "queryParams", ignore = true)
    @Mapping(target = "pathParams", ignore = true)
    @Mapping(target = "bodyParameter", ignore = true)
    @Mapping(target = "basePathParamNames", ignore = true)
    @Mapping(target = "returnTypeMapper", ignore = true)
    @Mapping(target = "options", ignore = true)
    TypeScriptServiceMethod mapMethod(ApiMethodModel methodModel, String httpMethod, String path, @Context Map<String, TypeScriptTypeAlias> typeAliases);

    TypeScriptServiceMethodParameter mapParameter(ApiMethodParameterModel parameterModel, @Context Map<String, TypeScriptTypeAlias> typeAliases);

    @AfterMapping
    default void adjustParameterNames(@MappingTarget TypeScriptService service) {
        var basePathParams = service.getBasePaths().get(0).getParams();

        for (var method : service.getMethods()) {
            var map = method.getPathParams()
                    .stream()
                    .collect(Collectors.toMap(TypeScriptServiceMethodParameter::getParameterName, TypeScriptServiceMethodParameter::getName));

            method.setBasePathParamNames(basePathParams.stream().map(map::get).toList());

            var path = method.getPath();
            for(var entry : map.entrySet()) {
                if(!Objects.equals(entry.getKey(), entry.getValue())) {
                    path = path.replaceParamName(entry.getKey(), entry.getValue());
                }
            }

            method.setPath(path);
        }
    }

    @AfterMapping
    default void setReturnTypeWrapper(ApiMethodModel methodModel, @MappingTarget TypeScriptServiceMethod method) {
        if (methodModel.getResponseType() == ApiMethodResponseType.STREAM) {
            method.setReturnTypeWrapper("Observable");
            method.setReturnTypeMapper("HTTP_DOWNLOAD_PIPE");
            method.setOptions("HTTP_DOWNLOAD_OPTIONS");
            method.setReturnType(TypeScriptType.DOWNLOAD_INFO);
        } else if (methodModel.getResponseType() == ApiMethodResponseType.SERVER_SEND_EVENT) {
            method.setReturnTypeWrapper("ServerSendEventSource");
            method.setReturnType(TypeScriptType.SERVER_SEND_EVENT);
        } else {
            if(method.getReturnType().getName().equals("string")) {
                method.setOptions("{responseType: 'text'}");
            }
            method.setReturnTypeWrapper("Promise");
        }
    }

    @AfterMapping
    default void setMethodParameters(ApiMethodModel methodModel, @MappingTarget TypeScriptServiceMethod method, @Context Map<String, TypeScriptTypeAlias> typeAliases) {
        var parameters = methodModel.getParameters().stream().collect(Collectors.groupingBy(ApiMethodParameterModel::getIn));

        method.setQueryParams(parameters.getOrDefault(ApiMethodParameterType.QUERY, Collections.emptyList()).stream().map(p -> mapParameter(p, typeAliases)).toList());
        method.setHeaderParams(parameters.getOrDefault(ApiMethodParameterType.HEADER, Collections.emptyList()).stream().map(p -> mapParameter(p, typeAliases)).toList());
        method.setPathParams(parameters.getOrDefault(ApiMethodParameterType.PATH, Collections.emptyList()).stream().map(p -> mapParameter(p, typeAliases)).toList());
        method.setFormData(!method.getQueryParams().isEmpty() && (method.getHttpMethod().equals("POST") || method.getHttpMethod().equals("PUT")));

        if (method.isFormData()) {
            method.setBodyParameter("formData");
        } else if (parameters.containsKey(ApiMethodParameterType.BODY)) {
            method.setBodyParameter(parameters.get(ApiMethodParameterType.BODY).get(0).getName());
        } else if (method.getHttpMethod().equals("POST") || method.getHttpMethod().equals("PUT")) {
            method.setBodyParameter("{}");
        }
    }
}
