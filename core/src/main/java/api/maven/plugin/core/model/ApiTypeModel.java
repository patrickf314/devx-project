package api.maven.plugin.core.model;

import api.maven.plugin.core.type.ApiTypeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiTypeModel {

    public static final ApiTypeModel UNKNOWN = new ApiTypeModel("unknown", ApiTypeType.UNKNOWN);

    private String name;
    private ApiTypeType type;
    private String className;
    private boolean required;
    private List<ApiTypeModel> typeArguments;
    private List<String> nesting;

    public ApiTypeModel(String name, ApiTypeType type) {
        this(name, type, null);
    }

    public ApiTypeModel(String name, ApiTypeType type, boolean required) {
        this(name, type, null, required);
    }

    public ApiTypeModel(String name, ApiTypeType type, boolean required, List<ApiTypeModel> typeArguments) {
        this(name, type, null, required, typeArguments, Collections.emptyList());
    }

    public ApiTypeModel(String name, ApiTypeType type, String className) {
        this(name, type, className, false);
    }

    public ApiTypeModel(String name, ApiTypeType type, String className, boolean required) {
        this(name, type, className, required, Collections.emptyList(), Collections.emptyList());
    }
}
