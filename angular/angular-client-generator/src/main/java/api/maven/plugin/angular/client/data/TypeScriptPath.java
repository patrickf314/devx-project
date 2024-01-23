package api.maven.plugin.angular.client.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TypeScriptPath {

    private String path;
    private List<String> params;

    public TypeScriptPath replaceParamName(String target, String replacement) {
        var newParams = new ArrayList<>(params);
        newParams.replaceAll(param -> param.equals(target) ? replacement : param);

        var newPath = path.replace("${" + target + "}", "${" + replacement + "}");
        var newThis = new TypeScriptPath();

        newThis.setParams(newParams);
        newThis.setPath(newPath);

        return newThis;
    }
}
