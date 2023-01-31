package api.maven.plugin.angular.client.data;

import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class TypeScriptService {

    private Collection<TypeScriptDependency> dependencies;
    private String className;
    private String name;

    private List<TypeScriptPath> basePaths;
    private List<TypeScriptServiceMethod> methods;

}
