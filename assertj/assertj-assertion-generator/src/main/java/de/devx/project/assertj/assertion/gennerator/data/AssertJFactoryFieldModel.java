package de.devx.project.assertj.assertion.gennerator.data;

import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import lombok.Data;

import java.util.Optional;

/**
 * Model of a public static {@code InstanceOfAssertFactory} field of another assertion class,
 * which is re-exported by the generated assertion class.
 */
@Data
public class AssertJFactoryFieldModel {

    private JavaClassModel assertionClass;

    /**
     * The name of the field in the {@link #assertionClass}
     */
    private String fieldName;

    /**
     * The type of the field, e.g. {@code InstanceOfAssertFactory<SectionDTO, SectionDTOAssert>}
     */
    private JavaTypeModel type;

    /**
     * Prefix used for the re-exported field, if its name clashes with another factory.
     * If not set, the prefix is derived from the package of the asserted type.
     */
    private String prefix;

    public Optional<String> getPrefix() {
        return Optional.ofNullable(prefix);
    }

    /**
     * @return the asserted type, i.e. the first type argument of the factory
     */
    public Optional<JavaTypeModel> getAssertedType() {
        return type.getTypeArguments().stream().findFirst();
    }
}
