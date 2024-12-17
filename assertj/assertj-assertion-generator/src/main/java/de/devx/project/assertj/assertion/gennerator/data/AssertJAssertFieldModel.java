package de.devx.project.assertj.assertion.gennerator.data;

import de.devx.project.commons.generator.model.JavaTypeModel;
import lombok.Data;

import static de.devx.project.commons.generator.model.JavaTypeModel.BOOLEAN;
import static de.devx.project.commons.generator.utils.StringUtils.capitalize;

@Data
public class AssertJAssertFieldModel {

    private String name;
    private JavaTypeModel type;

    public String getCapitalizedName() {
        return capitalize(name);
    }

    public String getGetterName(boolean javaRecord) {
        if(javaRecord) {
            return name;
        }

        if (BOOLEAN.equals(type)) {
            return "is" + getCapitalizedName();
        } else {
            return "get" + getCapitalizedName();
        }
    }
}
