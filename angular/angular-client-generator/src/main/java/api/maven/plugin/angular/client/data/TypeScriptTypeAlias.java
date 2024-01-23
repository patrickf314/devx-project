package api.maven.plugin.angular.client.data;

import lombok.Data;

@Data
public class TypeScriptTypeAlias {

    private String className;
    private String tsFile;
    private String tsType;
    private String tsPath;
    private String annotation;

}
