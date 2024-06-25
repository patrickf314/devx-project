package de.devx.project.assertj.condition.generator.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssertjConditionModel {

    private String packageName;
    private String className;
    private String generics = "";
    private String enclosingDTO;
    private List<AssertjClassFieldModel> fields = new ArrayList<>();

}
