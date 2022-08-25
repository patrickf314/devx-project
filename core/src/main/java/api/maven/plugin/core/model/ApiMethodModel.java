package api.maven.plugin.core.model;

import api.maven.plugin.core.type.ApiMethodResponseType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ApiMethodModel {

    private String name;

    private ApiTypeModel returnType;
    private ApiMethodResponseType responseType = ApiMethodResponseType.DEFAULT;
    private List<ApiMethodParameterModel> parameters;
    private List<String> httpMethods = new ArrayList<>();
    private List<String> paths = new ArrayList<>();

    public ApiMethodModel(String name) {
        this.name = name;
    }
}
