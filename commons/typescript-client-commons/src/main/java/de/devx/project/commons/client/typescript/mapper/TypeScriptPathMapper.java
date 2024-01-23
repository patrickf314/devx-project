package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiServiceEndpointModel;
import de.devx.project.commons.client.typescript.data.TypeScriptDependencyModel;
import de.devx.project.commons.client.typescript.data.TypeScriptPathModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodParameterModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceModel;
import de.devx.project.commons.client.typescript.io.TypeScriptTypeAlias;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mapper
public interface TypeScriptPathMapper {

    Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([a-zA-Z]+)}");

    default TypeScriptPathModel mapPath(String pathStr) {
        if("".equals(pathStr)) {
            return new TypeScriptPathModel(null, Collections.emptyList());
        }

        if(!pathStr.startsWith("/")) {
            pathStr = "/" + pathStr;
        }

        var pathMatcher = PATH_VARIABLE_PATTERN.matcher(pathStr);
        var params = new ArrayList<String>();

        while(pathMatcher.find()) {
            params.add(pathMatcher.group(1));
        }

        return new TypeScriptPathModel(pathMatcher.replaceAll("\\${$1}"), params);
    }
}
