<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="entityName" type="java.lang.String" -->
<#-- @ftlvariable name="generics" type="java.lang.String" -->
<#-- @ftlvariable name="imports" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="matcherFactoryFunctionName" type="java.lang.String" -->
<#-- @ftlvariable name="fields" type="java.util.List<api.maven.plugin.processor.data.EntityField>" -->
package ${packageName};

<#list imports as import>
import ${import};
</#list>

import org.hamcrest.Matcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsAnything;

import static org.hamcrest.core.Is.is;

public class ${entityName}Matcher${generics} extends TypeSafeMatcher<${entityName}${generics}> {

<#list fields as field>
    private final ${field.matcherType} ${field.name};
</#list>

    private ${entityName}Matcher() {
    <#list fields as field>
        this.${field.name} = new IsAnything<>();
    </#list>
    }

    private ${entityName}Matcher(${fields?map(field -> field.matcherType + " " + field.name)?join(", ")}) {
    <#list fields as field>
        this.${field.name} = ${field.name};
    </#list>
    }

    public static ${generics} ${entityName}Matcher${generics} ${matcherFactoryFunctionName}() {
        return new ${entityName}Matcher${generics}();
    }

    @Override
    protected boolean matchesSafely(${entityName} entity) {
        return ${fields?map(field -> field.name + ".matches(entity." + field.getter + "())")?join("\n\t\t\t&& ")};
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(${"\"<" + entityName + "(\""})
            ${fields?map(field -> ".appendText(\"" + field.name + "=\").appendDescriptionOf(" + field.name + ")")?join(".appendText(\", \")\n\t\t\t")}
            .appendText(${"\")>\""});
    }
<#list fields as field>

    public ${entityName}Matcher${generics} with${field.name?cap_first}(${field.type.fullType} ${field.name}) {
       return with${field.name?cap_first}(is(${field.name}));
    }

    public ${entityName}Matcher${generics} with${field.name?cap_first}(${field.matcherType} ${field.name}) {
        return new ${entityName}Matcher${generics}(${fields?map(field -> field.name)?join(", ")});
    }
</#list>
}