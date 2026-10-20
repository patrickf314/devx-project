package de.devx.project.assertj.assertion.gennerator.data;

import lombok.Value;

/**
 * Rendered {@code InstanceOfAssertFactory} field of a generated assertion class.
 */
@Value
public class AssertJFactoryDefinition {

    String name;
    String assertedType;
    String type;
    String initializer;
}
