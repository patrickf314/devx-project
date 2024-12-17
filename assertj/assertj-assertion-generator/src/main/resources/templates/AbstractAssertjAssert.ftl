<#-- @ftlvariable name="model" type="de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel" -->
<#-- @ftlvariable name="imports" type="java.util.List<java.lang.String>" -->
package ${model.packageName};

<#list imports as import>
import ${import};
</#list>

/**
 * Autogenerated abstract assert of the {@link ${model.typeName}}.
 * The abstract assert is used for extensions.
 * Use {@link ${model.name}Assert} as implementation.
 */
public class Abstract${model.name}Assert<<#list model.typeArguments as typeArgument>${typeArgument}, </#list>SELF extends Abstract${model.name}Assert<<#list model.typeArguments as typeArgument>${typeArgument}, </#list>SELF, ACTUAl>, ACTUAl extends ${model.typeName}<#if model.typeArguments?has_content><${model.typeArguments?join(", ")}></#if>>
    extends AbstractAssert<SELF, ACTUAl> {

    /**
     * Autogenerated constructor
     *
     * @param actual the actual value
     * @param selfType the class of the implementation of this abstract assert
     */
    protected Abstract${model.name}Assert(ACTUAl actual, Class<?> selfType) {
        super(actual, selfType);
    }
<#list model.fields as field>

    /**
     * Checks if ${field.name} of the actual value is equal to the given value.
     *
     * @param expected the expected value of field
     * @return this
     */
    public SELF has${field.capitalizedName}(${field.type.qualifiedTypeName} expected) {
        isNotNull();
        if (!Objects.equals(actual.${field.getGetterName(model.javaRecord)}(), expected)) {
            failWithActualExpectedAndMessage(actual.${field.getGetterName(model.javaRecord)}(), expected, "${model.name}.${field.name}");
        }
        return this.myself;
    }

    /**
     * Checks if ${field.name} of the actual value satisfies
     * all the assertions of the given consumer.
     *
     * @param consumer the consumer performing assertions
     * @return this
     */
    public SELF has${field.capitalizedName}Satisfying(Consumer<? super ${field.type.nonPrimitiveQualifiedTypeName}> consumer) {
        isNotNull();
        try {
            consumer.accept(actual.${field.getGetterName(model.javaRecord)}());
        } catch (AssertionError e) {
            throw new MultipleAssertionsError(new TextDescription("${model.name}.${field.name}"), List.of(e));
        }
        return this.myself;
    }
</#list>
}