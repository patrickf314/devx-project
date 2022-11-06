package api.maven.plugin.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ApiDTOModel {

    private String className;
    private String name;

    private ApiTypeModel extendedDTO;
    private Map<String, ApiTypeModel> fields = new HashMap<>();
    private List<String> typeArguments = new ArrayList<>();
    private ApiEnclosingDTOModel enclosingDTO;

    public ApiDTOModel(String className, String name) {
        this.className = className;
        this.name = name;
    }
}
