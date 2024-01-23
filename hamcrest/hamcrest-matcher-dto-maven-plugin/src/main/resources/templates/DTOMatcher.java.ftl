<#-- @ftlvariable name="packageName" type="java.lang.String" -->
<#-- @ftlvariable name="dtoName" type="java.lang.String" -->
<#-- @ftlvariable name="generics" type="java.lang.String" -->
<#-- @ftlvariable name="imports" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="matcherFactoryFunctionName" type="java.lang.String" -->
<#-- @ftlvariable name="fields" type="java.util.List<api.maven.plugin.dto.data.DTOField>" -->
package ${packageName};

<#list imports as import>
import ${import};
</#list>

import org.hamcrest.Matcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsAnything;

import static org.hamcrest.core.Is.is;

public class ${dtoName}Matcher${generics} extends TypeSafeMatcher<${dtoName}${generics}> {

<#list fields as field>
    private final ${field.matcherType} ${field.name};
</#list>

    private ${dtoName}Matcher() {
    <#list fields as field>
        this.${field.name} = new IsAnything<>();
    </#list>
    }

    private ${dtoName}Matcher(${fields?map(field -> field.matcherType + " " + field.name)?join(", ")}) {
    <#list fields as field>
        this.${field.name} = ${field.name};
    </#list>
    }

    public static ${generics} ${dtoName}Matcher${generics} ${matcherFactoryFunctionName}() {
        return new ${dtoName}Matcher${generics}();
    }

    @Override
    protected boolean matchesSafely(${dtoName} dto) {
        return ${fields?map(field -> field.name + ".matches(dto." + field.getter + "())")?join("\n\t\t\t&& ")};
    }

    @Override
    public void describeTo(Description d) {
        d.appendText(${"\"<" + dtoName + "(\""})
            ${fields?map(field -> ".appendText(\"" + field.name + "=\").appendDescriptionOf(" + field.name + ")")?join(".appendText(\", \")\n\t\t\t")}
            .appendText(${"\")>\""});
    }
<#list fields as field>

    public ${dtoName}Matcher${generics} with${field.name?cap_first}(${field.type.fullType} ${field.name}) {
       return with${field.name?cap_first}(is(${field.name}));
    }

    public ${dtoName}Matcher${generics} with${field.name?cap_first}(${field.matcherType} ${field.name}) {
        return new ${dtoName}Matcher${generics}(${fields?map(field -> field.name)?join(", ")});
    }
</#list>
}