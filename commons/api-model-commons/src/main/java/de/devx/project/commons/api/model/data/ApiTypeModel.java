package de.devx.project.commons.api.model.data;

import de.devx.project.commons.api.model.type.ApiTypeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiTypeModel {

    public static final ApiTypeModel UNKNOWN = new ApiTypeModel("unknown", ApiTypeType.UNKNOWN, null);

    private String name;
    private ApiTypeType type;
    private String className;
    private boolean required;
    private List<ApiTypeModel> typeArguments;
    private List<String> nesting;
    private List<String> annotations;

    public ApiTypeModel(String name, ApiTypeType type, boolean required) {
        this(name, type, null, required);
    }

    public ApiTypeModel(String name, ApiTypeType type, String className) {
        this(name, type, className, false);
    }

    public ApiTypeModel(String name, ApiTypeType type, String className, boolean required) {
        this(name, type, className, required, Collections.emptyList());
    }

    public ApiTypeModel(String name, ApiTypeType type, String className, boolean required, List<ApiTypeModel> typeArguments) {
        this(name, type, className, required, typeArguments, Collections.emptyList(), Collections.emptyList());
    }
}
