package api.maven.plugin.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ApiEnumModel {

    private String className;
    private String name;

    private List<String> values = new ArrayList<>();

    public ApiEnumModel(String className, String name) {
        this.className = className;
        this.name = name;
    }
}
