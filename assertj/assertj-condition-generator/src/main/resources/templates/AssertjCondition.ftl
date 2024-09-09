<#-- @ftlvariable name="condition" type="de.devx.project.assertj.condition.generator.data.AssertjConditionModel" -->
<#-- @ftlvariable name="imports" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="conditionFactoryFunctionName" type="java.lang.String" -->
package ${condition.packageName};

<#list imports as import>
    import ${import};
</#list>
import org.assertj.core.api.Condition;
import org.assertj.core.description.Description;
import org.assertj.core.description.JoinDescription;
import org.assertj.core.description.TextDescription;

import java.util.List;
import java.util.Objects;

public class ${condition.className}Condition${condition.generics} extends Condition<${condition.className}${condition.generics}> {

<#list condition.fields as field>
    private final ${field.conditionType} ${field.name};
</#list>

private ${condition.className}Condition() {
super("
<any ${condition.className}>");
    <#list condition.fields as field>
        this.${field.name} = null;
    </#list>
    }

    private ${condition.className}
    Condition(${condition.fields?map(field -> field.conditionType + " " + field.name)?join(", ")}) {
    super(new JoinDescription("${condition.className}[", "]", List.of(
    ${condition.fields?map(field -> "propertyDescription(\"" + field.name + "\", " + field.name + " == null ? null : " + field.name + ".description())")?join(",\n\t\t\t")}
    )));
    <#list condition.fields as field>
        this.${field.name} = ${field.name};
    </#list>
    }

    private static Description propertyDescription(String property, Description description) {
    return new TextDescription(property + "=" + (description == null ? "
    <any>" : description.value()));
        }

        public static ${condition.generics?has_content?then(condition.generics + ' ', '')}${condition.className}
        Condition${condition.generics} ${conditionFactoryFunctionName}() {
        return new ${condition.className}Condition${condition.generics}();
        }

        @Override
        public boolean matches(${condition.className}${condition.generics} item) {
        if(item == null) {
        return false;
        }

        return ${condition.fields?map(field -> "(this." + field.name + " == null || this." + field.name + ".matches(item." + field.getter + "()))")?join("\n\t\t\t&& ")}
        ;
        }
        <#list condition.fields as field>

            public ${condition.className}Condition${condition.generics} with${field.name?cap_first}(${field.type.fullType} ${field.name}) {
            return with${field.name?cap_first}(new Condition<>(actual -> Objects.equals(${field.name}, actual), Objects.toString(${field.name})));
            }

            public ${condition.className}Condition${condition.generics} with${field.name?cap_first}(${field.conditionType} ${field.name}) {
            return new ${condition.className}Condition${condition.generics}(${condition.fields?map(field -> field.name)?join(", ")});
            }
        </#list>
        }