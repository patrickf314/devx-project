package api.maven.plugin.core.io;

import api.maven.plugin.core.model.ApiModel;

import java.io.File;
import java.util.Optional;

public interface ApiModelExtractor {

    Optional<ApiModel> extract(File file);

}
