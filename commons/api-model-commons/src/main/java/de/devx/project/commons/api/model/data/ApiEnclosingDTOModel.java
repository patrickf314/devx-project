package de.devx.project.commons.api.model.data;

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
