package de.devx.project.commons.client.typescript.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class TypeScriptPathModel {

    private String path;
    private List<String> params;

    public TypeScriptPathModel replaceParamName(String target, String replacement) {
        if(this.path == null) {
            return this;
        }

        var newParams = new ArrayList<>(params);
        newParams.replaceAll(param -> param.equals(target) ? replacement : param);

        var newPath = path.replace("${" + target + "}", "${" + replacement + "}");
        return new TypeScriptPathModel(newPath, newParams);
    }
}
