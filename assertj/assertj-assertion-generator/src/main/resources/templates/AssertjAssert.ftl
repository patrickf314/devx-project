<#-- @ftlvariable name="model" type="de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel" -->
package ${model.packageName};

/**
 * Autogenerated assert of the {@link ${model.typeName}}.
 *
 * @see Abstract${model.name}Assert for extending this assert
 */
public final class ${model.name}Assert<#if model.typeArguments?has_content><${model.typeArguments?join(", ")}></#if> extends Abstract${model.name}Assert<<#list model.typeArguments as typeArgument>${typeArgument}, </#list>${model.name}Assert<#if model.typeArguments?has_content><${model.typeArguments?join(", ")}></#if>, ${model.typeName}<#if model.typeArguments?has_content><${model.typeArguments?join(", ")}></#if>> {

    /**
     * Autogenerated constructor
     *
     * @param actual the actual value
     */
    public ${model.name}Assert(${model.typeName}<#if model.typeArguments?has_content><${model.typeArguments?map(a -> a.definition)?join(", ")}></#if> actual) {
        super(actual, ${model.name}Assert.class);
    }
}