package de.devx.project.assertj.assertion.gennerator.data;

import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import de.devx.project.commons.generator.utils.ImportUtils;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Data
public class AssertJAssertModel {

    private String packageName;
    /**
     * The simple name of the class for which this model represents the Assert.
     * The Asserts will have the name Abstract{assertName} and {assertName}.
     */
    private String name;

    private List<AssertJAssertFieldModel> fields;

    private List<JavaTypeArgumentModel> typeArguments;

    private boolean javaRecord;

    private JavaTypeModel extendedAbstractAssertModel;

    /**
     * Getter for the name of the assert class.
     * For root level classes this is equal to {@link #getName()} + "Assert".
     * However, for nested classes the assert name contains $ signs, whereas
     * the name contains points.
     * <p>
     * E.g.: getAssertName(): TestEntity$NestedEntityAssert
     * getName(): TestEntity.NestedEntity
     *
     * @return the assert name
     */
    public String getAssertName() {
        return name.replace('.', '$') + "Assert";
    }

    public Stream<String> asJavaImport(String currentPackage) {
        var i = name.indexOf('.');
        var rootName = i == -1 ? name : name.substring(0, i);
        return Stream.concat(
                ImportUtils.asJavaImport(currentPackage, packageName, rootName).stream(),
                typeArguments.stream().flatMap(t -> t.streamImports(currentPackage))
        );
    }

    public String getFactoryName() {
        return name.replace('.', '$')
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase(Locale.ROOT);
    }
}
