package de.devx.project.assertj.condition.generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssertjClassFieldModel {

    private AssertjClassFieldTypeModel type;
    private String name;
    private String getter;

    public String getConditionType() {
        if (type.getKind() == AssertjClassFieldTypeModel.Kind.PRIMARY) {
            return "Condition<" + type.getClassName() + ">";
        }

        return "Condition<" + type.getFullType() + ">";
    }
}
