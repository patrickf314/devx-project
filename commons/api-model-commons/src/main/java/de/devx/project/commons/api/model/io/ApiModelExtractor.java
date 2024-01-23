package de.devx.project.commons.api.model.io;

import de.devx.project.commons.api.model.data.ApiModel;

import java.io.File;
import java.util.Optional;

public interface ApiModelExtractor {

    Optional<ApiModel> extract(File file);

}
