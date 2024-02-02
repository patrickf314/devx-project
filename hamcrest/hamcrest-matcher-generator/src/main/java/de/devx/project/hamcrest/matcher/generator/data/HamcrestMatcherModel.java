package de.devx.project.hamcrest.matcher.generator.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HamcrestMatcherModel {

    private String packageName;
    private String className;
    private String generics = "";
    private String enclosingDTO;
    private List<HamcrestClassFieldModel> fields = new ArrayList<>();

}
