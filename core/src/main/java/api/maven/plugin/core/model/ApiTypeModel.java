package api.maven.plugin.core.model;

import api.maven.plugin.core.type.ApiTypeType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ApiTypeModel {

    public static final ApiTypeModel UNKNOWN = new ApiTypeModel("unknown", ApiTypeType.UNKNOWN);

    private String name;
    private ApiTypeType type;
    private String className;
    private boolean required;
    private List<ApiTypeModel> typeArguments = new ArrayList<>();

    public ApiTypeModel(String name, ApiTypeType type) {
        this(name, type, null);
    }

    public ApiTypeModel(String name, ApiTypeType type, boolean required) {
        this(name, type, null);

        this.required = required;
    }

    public ApiTypeModel(String name, ApiTypeType type, String className) {
        this.name = name;
        this.type = type;
        this.className = className;
    }
}
