package de.devx.project.commons.api.model.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiBrandedTypeModel {

    private String className;
    private String name;
    private ApiTypeModel underlyingType;

    public ApiBrandedTypeModel(String className, String name, ApiTypeModel underlyingType) {
        this.className = className;
        this.name = name;
        this.underlyingType = underlyingType;
    }
}
