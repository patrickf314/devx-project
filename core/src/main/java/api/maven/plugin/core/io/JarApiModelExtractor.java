package api.maven.plugin.core.io;

import api.maven.plugin.core.ApiModelConstants;
import api.maven.plugin.core.model.ApiModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

public class JarApiModelExtractor implements ApiModelExtractor {

    public Optional<ApiModel> extract(File file) {
        try (var classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()})) {
            var inputStream = classLoader.getResourceAsStream(ApiModelConstants.FILE_NAME);
            if(inputStream == null) {
                return Optional.empty();
            }

            try (var reader = new ApiModelReader(inputStream)) {
                return Optional.of(reader.read());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract file " + file.getAbsolutePath(), e);
        }
    }
}
