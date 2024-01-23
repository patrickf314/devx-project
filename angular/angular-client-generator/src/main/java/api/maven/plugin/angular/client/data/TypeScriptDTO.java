package api.maven.plugin.angular.client.data;

import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
public class TypeScriptDTO {

    private Collection<TypeScriptDependency> dependencies;
    private String className;
    private String name;
    private List<String> typeArguments;
    private TypeScriptType extendedDTO;
    private Map<String, TypeScriptType> fields;

}
