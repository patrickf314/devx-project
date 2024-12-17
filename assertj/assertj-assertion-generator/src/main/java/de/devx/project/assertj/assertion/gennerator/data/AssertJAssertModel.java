package de.devx.project.assertj.assertion.gennerator.data;

import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import de.devx.project.commons.generator.utils.ImportUtils;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class AssertJAssertModel {

    private String packageName;
    /**
     * The simple name of the class for which this model represents the Assert.
     * The Asserts will have the name Abstract{name}Assert and {name}Assert.
     */
    private String name;

    private List<AssertJAssertFieldModel> fields;

    private List<JavaTypeArgumentModel> typeArguments;

    private boolean javaRecord;

    /**
     * Getter for the name as type.
     * For root level classes this is equal to {@link #getName()}.
     * However, for nested classes the name contains $ signs, whereas
     * the type name contains points.
     * <p>
     * E.g.: getName(): TestEntity$NestedEntity
     * getTypeName(): TestEntity.NestedEntity
     *
     * @return the type name
     */
    public String getTypeName() {
        return name.replace('$', '.');
    }

    public Optional<String> asJavaImport(String currentPackage) {
        var i = name.indexOf('$');
        var rootName = i == -1 ? name : name.substring(0, i);
        return ImportUtils.asJavaImport(currentPackage, packageName, rootName);
    }
}
