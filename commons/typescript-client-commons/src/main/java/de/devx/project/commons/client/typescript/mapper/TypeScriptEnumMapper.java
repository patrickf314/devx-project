package de.devx.project.commons.client.typescript.mapper;

import de.devx.project.commons.api.model.data.ApiEnumModel;
import de.devx.project.commons.client.typescript.data.TypeScriptEnumModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TypeScriptEnumMapper {

    TypeScriptEnumModel mapEnum(ApiEnumModel model);

}
