package api.maven.plugin.angular.client.utils;

import api.maven.plugin.core.model.ApiDTOModel;
import api.maven.plugin.core.model.ApiEnumModel;
import api.maven.plugin.core.model.ApiServiceEndpointModel;
import api.maven.plugin.core.model.ApiTypeModel;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class TypeScriptOutputDirectory {

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
        var file = new File(serviceFolder(TypeScriptServiceUtils.getServiceByClassName(endpointModel.getClassName())), serviceFileName(endpointModel) + ".ts");
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create dto file " + file.getAbsolutePath());
        }
        return file;
    }

    public String serviceFileName(ApiServiceEndpointModel endpointModel) {
        return serviceFileName(endpointModel.getName());
    }

    private static String serviceFileName(String name) {
        if(name.endsWith("ServiceAPI")) {
            name = name.substring(0, name.length() - 10);
        }

        return TypeScriptUtils.toLowerCaseName(name) + ".service";
    }

    public File dtoFolder(String serviceName) throws IOException {
        return dtoFolder(serviceFolder(serviceName));
    }

    public File dtoFolder(File serviceFolder) throws IOException {
        return ensureFolderExists(new File(serviceFolder, "dto"), "dto");
    }

    public File dtoFile(ApiDTOModel dtoModel) throws IOException {
        var file = new File(dtoFolder(TypeScriptServiceUtils.getServiceByClassName(dtoModel.getClassName())), dtoFileName(dtoModel) + ".ts");
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create dto file " + file.getAbsolutePath());
        }
        return file;
    }

    public String dtoFileName(ApiDTOModel dtoModel) {
        return dtoFileName(dtoModel.getName());
    }

    private String dtoFileName(String name) {
        if(name.endsWith("DTO")) {
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
        var file = new File(enumFolder(TypeScriptServiceUtils.getServiceByClassName(enumModel.getClassName())), enumFileName(enumModel) + ".ts");
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
        return relativePath(TypeScriptServiceUtils.getServiceByClassName(endpointModel.getClassName()), null, targetType);
    }

    public String relativePathToCommonsFile(ApiServiceEndpointModel endpointModel, String fileName) {
        return relativePath(TypeScriptServiceUtils.getServiceByClassName(endpointModel.getClassName()), null, COMMONS_FOLDER_NAME, null) + fileName;
    }

    public String relativePath(ApiDTOModel dtoModel, ApiTypeModel targetType) {
        return relativePath(TypeScriptServiceUtils.getServiceByClassName(dtoModel.getClassName()), "dto", targetType);
    }

    private String relativePath(String currentService, String currentSubFolder, ApiTypeModel targetType) {
        return switch (targetType.getType()) {
            case ENUM -> relativePath(currentService, currentSubFolder, TypeScriptServiceUtils.getServiceByClassName(targetType.getClassName()), "type") + enumFileName(targetType.getName());
            case DTO -> relativePath(currentService, currentSubFolder, TypeScriptServiceUtils.getServiceByClassName(targetType.getClassName()), "dto") + dtoFileName(targetType.getName());
            default -> throw new IllegalArgumentException("Cannot get relative path to type " + targetType.getType().name());
        };
    }

    private String relativePath(String currentService, String currentSubFolder, String targetService, String targetSubFolder) {

        if (!targetService.equals(currentService)) {
            return (currentSubFolder == null ? "../" : "../../") + targetService + (targetSubFolder == null ? "/" : "/" + targetSubFolder + "/");
        } else if (Objects.equals(targetSubFolder, currentSubFolder)) {
            return "./";
        } else if (currentSubFolder == null) {
            return "./" + targetSubFolder + "/";
        } else {
            return "../" + targetSubFolder + "/";
        }
    }

    private File ensureFolderExists(File folder, String type) throws IOException {
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Failed to create " + type + " folder " + folder.getAbsolutePath());
        }
        return folder;
    }

}
