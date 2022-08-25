package api.maven.plugin.angular.client.data;

import lombok.Data;

import java.util.List;

@Data
public class TypeScriptEnum {

    private String className;
    private String name;
    private List<String> values;

}
