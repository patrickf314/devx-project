package de.devx.project.hamcrest.matcher.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HamcrestClassFieldModel {

    private HamcrestClassFieldTypeModel type;
    private String name;
    private String getter;

    public String getMatcherType() {
        if (type.getKind() == HamcrestClassFieldTypeModel.Kind.PRIMARY) {
            return "Matcher<" + type.getClassName() + ">";
        }

        return "Matcher<" + type.getFullType() + ">";
    }
}
