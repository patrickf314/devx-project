package api.maven.plugin.angular.client.data;

import lombok.Data;

import java.util.Set;

@Data
public class TypeScriptDependency {

    private String path;
    private Set<String> identifiers;

}
