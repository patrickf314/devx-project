package de.devx.project.commons.client.typescript.io;

import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiServiceEndpointModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class TypeScriptRelativePaths {

    private final SourceFileGenerator fileGenerator;
    private final Function<String, String> packageNameExtractor;

    public String get(ApiServiceEndpointModel endpointModel, ApiTypeModel targetType) {
        return relativePath(packageNameExtractor.apply(endpointModel.getClassName()), Collections.emptyList(), targetType);
    }

    public String get(ApiDTOModel dtoModel, ApiTypeModel targetType) {
        var nesting = new ArrayList<String>();
        nesting.add("dto");
        return relativePath(packageNameExtractor.apply(dtoModel.getClassName()), nesting, targetType);
    }

    private String relativePath(String currentService, List<String> currentSubFolder, ApiTypeModel targetType) {
        var nesting = new ArrayList<String>();

        var fileName = fileGenerator.fileName(targetType.getClassName());
        switch (targetType.getType()) {
            case ENUM -> nesting.add("type");
            case DTO -> nesting.add("dto");
            default ->
                    throw new IllegalArgumentException("Cannot get relative path to type " + targetType.getType().name());
        }

        return relativePath(currentService, currentSubFolder, packageNameExtractor.apply(targetType.getClassName()), nesting) + fileName;
    }

    private String relativePath(String currentService, List<String> currentSubFolder, String targetService, List<String> targetSubFolder) {
        var builder = new StringBuilder();
        var depthDiff = currentSubFolder.size() - targetSubFolder.size();
        if (depthDiff > 0) {
            IntStream.range(0, depthDiff).mapToObj(i -> "../").forEach(builder::append);
            depthDiff = 0;
        }

        var parentFolderMissmatch = !targetService.equals(currentService);
        for (var i = 0; i < targetSubFolder.size() + depthDiff; i++) {
            if (parentFolderMissmatch) {
                builder.append("../");
                continue;
            }

            if (!targetSubFolder.get(i).equals(currentSubFolder.get(i))) {
                builder.append("../");
                parentFolderMissmatch = true;
            }
        }

        if (!targetService.equals(currentService)) {
            builder.append("../").append(targetService).append("/");
        }

        if (builder.isEmpty()) {
            builder.append("./");
        }

        parentFolderMissmatch = !targetService.equals(currentService);
        for (var i = 0; i < targetSubFolder.size(); i++) {
            if (i >= currentSubFolder.size() || parentFolderMissmatch) {
                builder.append(targetSubFolder.get(i)).append("/");
                continue;
            }

            if (!currentSubFolder.get(i).equals(targetSubFolder.get(i))) {
                builder.append(targetSubFolder.get(i)).append("/");
                parentFolderMissmatch = true;
            }
        }

        return builder.toString();
    }

}
