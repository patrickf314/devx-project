package api.maven.plugin.angular.client;

import api.maven.plugin.angular.client.data.TypeScriptDependency;
import api.maven.plugin.angular.client.data.TypeScriptTypeAlias;
import api.maven.plugin.angular.client.utils.TypeScriptOutputDirectory;
import api.maven.plugin.core.model.ApiServiceEndpointModel;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReactClientGenerator extends TypeScriptClientGenerator {

    public ReactClientGenerator(String outputDirectory, List<TypeScriptTypeAlias> typeAliases) throws IOException {
        super(new TypeScriptOutputDirectory(outputDirectory) {
            @Override
            protected String getServiceByClassName(String className) {
                if(!className.startsWith("de.badminton.neubiberg.backend.api.")) {
                    return "common";
                }

                var i = "de.badminton.neubiberg.backend.api.".length();
                var j = className.indexOf('.', i);
                if(j == -1) {
                    return "common";
                }

                return className.substring(i, j);
            }
        }, typeAliases);
    }

    @Override
    protected void generateEndpoint(ApiServiceEndpointModel endpointModel) throws IOException {
        var file = outputDirectory.serviceFile(endpointModel);
        var template = configuration.getTemplate("react-service-template.ts.ftl");

        try (var writer = new FileWriter(file, false)) {
            template.process(Map.of("model", MAPPER.mapService(endpointModel, findDependencies(endpointModel), typeAliases)), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    protected Collection<TypeScriptDependency> findDependencies(ApiServiceEndpointModel endpointModel) throws IOException {
        return findDependencies(endpointModel, Map.of(
                "../../config", Set.of("Config"),
                "@reduxjs/toolkit", Set.of("createAsyncThunk"),
                "../../common/service/service-commons", Set.of("prepareHeaders", "mapJsonResponse", "mapVoidResponse", "ThunkOptions", "url"),
                "../../slice/app-store", Set.of("AppState", "useAppDispatch"),
                "react", Set.of("useMemo")
        ));
    }
}
