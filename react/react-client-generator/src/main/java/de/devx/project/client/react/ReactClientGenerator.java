package de.devx.project.client.react;

import de.devx.project.client.react.properties.ReactClientGeneratorProperties;
import de.devx.project.commons.client.typescript.TypescriptClientGenerator;
import de.devx.project.commons.client.typescript.data.TypeScriptImportModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodParameterModel;
import de.devx.project.commons.client.typescript.data.TypeScriptServiceModel;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.generator.io.SourceFileGenerator;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ReactClientGenerator extends TypescriptClientGenerator<ReactClientGeneratorProperties> {

    private static final String THUNK_OPTIONS = "ThunkOptions";
    private static final String SERVICE_COMMONS = "ReactServiceCommons";
    private static final String DOWNLOAD_STREAM = "DownloadStreamDTO";

    public ReactClientGenerator(SourceFileGenerator fileGenerator,
                                ReactClientGeneratorProperties properties) {
        super(fileGenerator, properties);
    }

    public void generateService(TypeScriptServiceModel model) throws IOException {
        if (generatedClasses.contains(model.getClassName())) {
            return;
        }

        generatedClasses.add(model.getClassName());

        var packageName = properties.getPackageNameForClass(model.getClassName());
        var imports = resolveImports(packageName, model, properties.getBackendUrlGetter(), properties.getHttpHeaderCustomizer(),
                new TypeScriptDependency("State", utilityPackage() + "/thunk-options.ts"),
                new TypeScriptDependency("ThunkOptions", utilityPackage() + "/thunk-options.ts"),
                new TypeScriptDependency("Dispatch", utilityPackage() + "/thunk-options.ts"),
                new TypeScriptDependency("url", utilityPackage() + "/react-service-commons.ts"),
                new TypeScriptDependency("mapJsonResponse", utilityPackage() + "/react-service-commons.ts"),
                new TypeScriptDependency("mapVoidResponse", utilityPackage() + "/react-service-commons.ts"),
                new TypeScriptDependency("mapStringResponse", utilityPackage() + "/react-service-commons.ts"),
                new TypeScriptDependency("mapStreamingResponse", utilityPackage() + "/react-service-commons.ts"),
                new TypeScriptDependency("DownloadStreamDTO", utilityPackage() + "/download-stream.dto.ts")
        );

        processTemplate("react-service-template.ts.ftl", packageName, model.getName(), Map.of(
                "model", model,
                "imports", imports,
                "backendUrl", properties.getBackendUrl() == null ? "" : properties.getBackendUrl(),
                "backendUrlGetterIdentifier", properties.getBackendUrlGetter() == null ? "" : properties.getBackendUrlGetter().getIdentifier(),
                "prepareHeadersIdentifier", properties.getHttpHeaderCustomizer() == null ? "" : properties.getHttpHeaderCustomizer().getIdentifier()
        ));
    }

    public void generateUtilities() throws IOException {
        var packageName = utilityPackage().replace('/', '.');
        if (!generatedClasses.contains(packageName + "." + DOWNLOAD_STREAM)) {
            generatedClasses.add(packageName + "." + DOWNLOAD_STREAM);
            processTemplate("download-stream.dto.ts.ftl", packageName, DOWNLOAD_STREAM, Map.of());
        }

        if (!generatedClasses.contains(packageName + "." + THUNK_OPTIONS)) {
            generatedClasses.add(packageName + "." + THUNK_OPTIONS);
            processTemplate("thunk-options.ts.ftl", packageName, THUNK_OPTIONS, Map.of(
                    "imports", resolveImports(packageName, properties.getReduxThunkConfig(), properties.getErrorSerializer()),
                    "reduxThunkConfigIdentifier", properties.getReduxThunkConfig().getIdentifier(),
                    "errorSerializerIdentifier", properties.getErrorSerializer().getIdentifier()
            ));
        }

        if (!generatedClasses.contains(packageName + "." + SERVICE_COMMONS)) {
            generatedClasses.add(packageName + "." + SERVICE_COMMONS);
            processTemplate("react-service-commons.ts.ftl", packageName, SERVICE_COMMONS, Map.of(
                    "imports", resolveImports(packageName, properties.getErrorMapper()),
                    "errorMapperIdentifier", properties.getErrorMapper().getIdentifier()
            ));
        }
    }

    private String utilityPackage() {
        return properties.getDefaultPackageAlias() == null ? "commons" : properties.getDefaultPackageAlias();
    }

    private Collection<TypeScriptImportModel> resolveImports(String currentPackage, TypeScriptServiceModel model, TypeScriptDependency... dependencies) {
        var imports = new HashMap<String, TypeScriptImportModel>();
        resolveImports(currentPackage, dependencies).forEach(i -> addImport(i, imports));
        model.getMethods()
                .stream()
                .map(method -> importModelsForMethod(currentPackage, method, model.getClassName()))
                .flatMap(List::stream)
                .forEach(i -> addImport(i, imports));
        return imports.values();
    }

    private List<TypeScriptImportModel> importModelsForMethod(String currentPackage, TypeScriptServiceMethodModel model, String currentClassName) {
        return Stream.concat(
                model.getParameters()
                        .stream()
                        .map(TypeScriptServiceMethodParameterModel::getType)
                        .map(type -> importModelsForType(currentPackage, type, currentClassName))
                        .flatMap(List::stream),
                importModelsForType(currentPackage, model.getReturnType(), currentClassName).stream()
        ).toList();
    }
}
