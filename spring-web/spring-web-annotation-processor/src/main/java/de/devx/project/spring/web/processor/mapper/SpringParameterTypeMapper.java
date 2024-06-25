package de.devx.project.spring.web.processor.mapper;

import de.devx.project.commons.api.model.type.ApiMethodParameterType;
import de.devx.project.commons.processor.spring.type.ParameterType;
import org.mapstruct.Mapper;

@Mapper
public interface SpringParameterTypeMapper {

    ApiMethodParameterType mapParameterType(ParameterType type);

}
