package de.devx.project.commons.client.typescript.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
public class TypeScriptTypeModel {

    private String name;
    private String className;
    private boolean optional;

    private Set<TypeScriptTypeModel> dependentTypes;

    private String zodSchema;

    public Set<String> getDependentClassNames() {
        return Stream.concat(
                Optional.ofNullable(className).stream(),
                dependentTypes.stream().map(TypeScriptTypeModel::getDependentClassNames).flatMap(Set::stream)
        ).collect(Collectors.toSet());
    }
}
