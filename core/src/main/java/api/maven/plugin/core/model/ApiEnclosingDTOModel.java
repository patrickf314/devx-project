package api.maven.plugin.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiEnclosingDTOModel {

    private String className;
    private String name;
    private List<String> nesting;

}
