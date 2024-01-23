package api.maven.plugin.angular.client.utils;

import de.devx.project.commons.api.model.data.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class TypeScriptOutputDirectory {

    private static final String COMMONS_FOLDER_NAME = "commons";
    private final File outputDirectory;

    public TypeScriptOutputDirectory(String outputDirectory) throws IOException {
        this.outputDirectory = ensureFolderExists(new File(outputDirectory), "output");
    }

    public File commonsFolder() throws IOException {
        return ensureFolderExists(new File(outputDirectory, COMMONS_FOLDER_NAME), COMMONS_FOLDER_NAME);
    }

    public File commonsFile(String fileName) throws IOException {
        var file = new File(commonsFolder(), fileName);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create commons file " + file.getAbsolutePath());
        }
        return file;
    }

    public File serviceFolder(String serviceName) throws IOException {
        return ensureFolderExists(new File(outputDirectory, serviceName), "service");
    }

    public File serviceFile(ApiServiceEndpointModel endpointModel) throws IOException {
        var file = new File(serviceFolder(getServiceByClassName(endpointModel.getClassName())), serviceFileName(endpointModel) + ".ts");
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create dto file " + file.getAbsolutePath());
        }
        return file;
    }

    public String serviceFileName(ApiServiceEndpointModel endpointModel) {
        return serviceFileName(endpointModel.getName());
    }

    private static String serviceFileName(String name) {
        if (name.endsWith("ServiceAPI")) {
            name = name.substring(0, name.length() - 10);
        }

        return TypeScriptUtils.toLowerCaseName(name) + ".service";
    }

    public File dtoFolder(String serviceName, ApiEnclosingDTOModel enclosingDTO) throws IOException {
        return dtoFolder(serviceFolder(serviceName), enclosingDTO);
    }

    public File dtoFolder(File serviceFolder, ApiEnclosingDTOModel enclosingDTO) throws IOException {
        var dtoFolderName = "dto";

        if (enclosingDTO != null) {
            if (!enclosingDTO.getNesting().isEmpty()) {
                dtoFolderName += "/" + enclosingDTO.getNesting().stream().map(this::enclosingFolderName).collect(Collectors.joining("/"));
            }

            dtoFolderName += "/" + enclosingFolderName(enclosingDTO.getName());
        }

        return ensureFolderExists(new File(serviceFolder, dtoFolderName), "dto");
    }

    public File dtoFile(ApiDTOModel dtoModel) throws IOException {
        var file = new File(dtoFolder(getServiceByClassName(dtoModel.getClassName()), dtoModel.getEnclosingDTO()), dtoFileName(dtoModel) + ".ts");
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create dto file " + file.getAbsolutePath());
        }
        return file;
    }

    public String dtoFileName(ApiDTOModel dtoModel) {
        return dtoFileName(dtoModel.getName());
    }

    private String dtoFileName(String name) {
        if (name.endsWith("DTO")) {
            name = name.substring(0, name.length() - 3);
        }

        return TypeScriptUtils.toLowerCaseName(name + ".dto");
    }

    public File enumFolder(String serviceName) throws IOException {
        return enumFolder(serviceFolder(serviceName));
    }

    public File enumFolder(File serviceFolder) throws IOException {
        return ensureFolderExists(new File(serviceFolder, "type"), "type");
    }

    public File enumFile(ApiEnumModel enumModel) throws IOException {
        var file = new File(enumFolder(getServiceByClassName(enumModel.getClassName())), enumFileName(enumModel) + ".ts");
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create dto file " + file.getAbsolutePath());
        }
        return file;
    }

    public String enumFileName(ApiEnumModel model) {
        return enumFileName(model.getName());
    }

    private String enumFileName(String name) {
        return TypeScriptUtils.toLowerCaseName(name.substring(0, name.length() - 4)) + ".type";
    }

    public String relativePath(ApiServiceEndpointModel endpointModel, ApiTypeModel targetType) {
        return relativePath(getServiceByClassName(endpointModel.getClassName()), Collections.emptyList(), targetType);
    }

    public String relativePathToCommonsFile(ApiServiceEndpointModel endpointModel, String fileName) {
        return relativePath(getServiceByClassName(endpointModel.getClassName()), Collections.emptyList(), COMMONS_FOLDER_NAME, Collections.emptyList()) + fileName;
    }

    public String relativePath(ApiDTOModel dtoModel, ApiTypeModel targetType) {
        var nesting = new ArrayList<String>();

        nesting.add("dto");
        if (dtoModel.getEnclosingDTO() != null) {
            nesting.addAll(dtoModel.getEnclosingDTO().getNesting().stream().map(this::enclosingFolderName).toList());
            nesting.add(enclosingFolderName(dtoModel.getEnclosingDTO().getName()));
        }

        return relativePath(getServiceByClassName(dtoModel.getClassName()), nesting, targetType);
    }

    protected abstract String getServiceByClassName(String className);

    private String relativePath(String currentService, List<String> currentSubFolder, ApiTypeModel targetType) {
        var nesting = new ArrayList<String>();

        String fileName;
        switch (targetType.getType()) {
            case ENUM -> {
                nesting.add("type");
                fileName = enumFileName(targetType.getName());
            }
            case DTO -> {
                nesting.add("dto");
                fileName = dtoFileName(targetType.getName());
            }
            default ->
                    throw new IllegalArgumentException("Cannot get relative path to type " + targetType.getType().name());
        }

        nesting.addAll(targetType.getNesting().stream().map(this::enclosingFolderName).toList());

        return relativePath(currentService, currentSubFolder, getServiceByClassName(targetType.getClassName()), nesting) + fileName;
    }

    private String relativePath(String currentService, List<String> currentSubFolder, String targetService, List<String> targetSubFolder) {
        var builder = new StringBuilder();
        var depthDiff = currentSubFolder.size() - targetSubFolder.size();
        if(depthDiff > 0) {
            IntStream.range(0, depthDiff).mapToObj(i -> "../").forEach(builder::append);
            depthDiff = 0;
        }

        var parentFolderMissmatch = !targetService.equals(currentService);
        for(var i = 0; i < targetSubFolder.size() + depthDiff; i ++) {
            if(parentFolderMissmatch) {
                builder.append("../");
                continue;
            }

            if(!targetSubFolder.get(i).equals(currentSubFolder.get(i))) {
                builder.append("../");
                parentFolderMissmatch = true;
            }
        }

        if (!targetService.equals(currentService)) {
            builder.append("../").append(targetService).append("/");
        }

        if(builder.isEmpty()) {
            builder.append("./");
        }

        parentFolderMissmatch = !targetService.equals(currentService);
        for(var i = 0; i < targetSubFolder.size(); i ++) {
            if(i >= currentSubFolder.size() || parentFolderMissmatch) {
                builder.append(targetSubFolder.get(i)).append("/");
                continue;
            }

            if(!currentSubFolder.get(i).equals(targetSubFolder.get(i))) {
                builder.append(targetSubFolder.get(i)).append("/");
                parentFolderMissmatch = true;
            }
        }

        return builder.toString();
    }

    private File ensureFolderExists(File folder, String type) throws IOException {
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Failed to create " + type + " folder " + folder.getAbsolutePath());
        }
        return folder;
    }

    private String enclosingFolderName(String name) {
        if (name.endsWith("DTO")) {
            name = name.substring(0, name.length() - 3);
        }

        return TypeScriptUtils.toLowerCaseName(name);
    }

}
