package api.maven.plugin.core.io;

import api.maven.plugin.core.model.ApiModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class ApiModelFileExtractor implements ApiModelExtractor {

    public Optional<ApiModel> extract(File file) {
        try (var inputStream = new FileInputStream(file)) {
            try (var reader = new ApiModelReader(inputStream)) {
                return Optional.of(reader.read());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract file " + file.getAbsolutePath(), e);
        }
    }
}
