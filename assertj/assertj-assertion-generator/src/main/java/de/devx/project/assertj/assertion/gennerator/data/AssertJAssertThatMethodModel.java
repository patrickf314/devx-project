package de.devx.project.assertj.assertion.gennerator.data;

import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import lombok.Data;

import java.util.List;

@Data
public class AssertJAssertThatMethodModel {

    private JavaClassModel assertionClass;
    /**
     * The type of the asserted object
     */
    private JavaTypeModel type;

    private JavaTypeModel assertType;

    private List<JavaTypeArgumentModel> typeArguments;

}
