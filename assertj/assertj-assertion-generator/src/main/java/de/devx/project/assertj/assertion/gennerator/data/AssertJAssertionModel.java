package de.devx.project.assertj.assertion.gennerator.data;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class AssertJAssertionModel {

    private String packageName;
    private String name;
    private Set<AssertJAssertModel> asserts = new LinkedHashSet<>();
    private Set<AssertJAssertThatMethodModel> assertThatMethods = new LinkedHashSet<>();
    private Set<AssertJFactoryFieldModel> factoryFields = new LinkedHashSet<>();
}
