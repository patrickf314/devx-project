package de.devx.project.commons.api.model.io;

import de.devx.project.commons.api.model.ApiModelConstants;
import de.devx.project.commons.api.model.data.ApiModel;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipFile;

public class JarApiModelExtractor implements ApiModelExtractor {

    public Optional<ApiModel> extract(File file) {
        try (var zipFile = new ZipFile(file)) {
            var entry = zipFile.getEntry(ApiModelConstants.FILE_NAME);
            if (entry == null) {
                return Optional.empty();
            }

            try (var inputStream = zipFile.getInputStream(entry);
                 var reader = new ApiModelReader(inputStream)) {
                return Optional.of(reader.read());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract file " + file.getAbsolutePath(), e);
        }
    }
}
