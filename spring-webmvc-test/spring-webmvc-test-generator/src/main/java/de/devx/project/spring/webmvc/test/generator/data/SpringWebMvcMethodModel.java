package de.devx.project.spring.webmvc.test.generator.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpringWebMvcMethodModel {

    private String name;
    private SpringWebMvcPathModel path;
    private SpringWebMvcTypeModel returnType;
    private String httpMethod;
    private boolean defaultServiceCall;

    // All parameters
    private List<SpringWebMvcParameterModel> parameters = new ArrayList<>();

    // Parameters separated by their location
    public List<SpringWebMvcParameterModel> getHeaderParams() {
        return getParamsByType(SpringWebMvcParameterModel.Type.HEADER);
    }

    public List<SpringWebMvcParameterModel> getQueryParams() {
        return getParamsByType(SpringWebMvcParameterModel.Type.QUERY);
    }

    public List<SpringWebMvcParameterModel> getPathParams() {
        return getParamsByType(SpringWebMvcParameterModel.Type.PATH);
    }

    public SpringWebMvcParameterModel getBodyParam() {
        var params = getParamsByType(SpringWebMvcParameterModel.Type.BODY);
        return params.isEmpty() ? null : params.get(0);
    }

    private List<SpringWebMvcParameterModel> getParamsByType(SpringWebMvcParameterModel.Type path) {
        return parameters.stream().filter(p -> p.getIn() == path).toList();
    }

    public String getCapitalizedName() {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
