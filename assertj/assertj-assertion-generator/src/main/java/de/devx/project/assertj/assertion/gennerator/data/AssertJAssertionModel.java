package de.devx.project.assertj.assertion.gennerator.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssertJAssertionModel {

    private String packageName;
    private String name;
    private List<AssertJAssertModel> asserts = new ArrayList<>();
    private List<AssertJAssertThatMethodModel> assertThatMethods = new ArrayList<>();
}
