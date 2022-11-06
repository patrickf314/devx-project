<#-- @ftlvariable name="model" type="api.maven.plugin.angular.client.data.TypeScriptService" -->
<#macro setHeaderParam param>
<#-- @ftlvariable name="param" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethodParameter" -->
    <#if param.type.name == "string" || param.type.name == "string[]" >
        headers['${param.parameterName}'] = ${param.name};
    <#elseif param.type.name == "number" || param.type.name == "boolean">
        headers['${param.parameterName}'] = String(${param.name});
    <#elseif param.type.name == "number[]"  || param.type.name == "boolean[]" >
        headers['${param.parameterName}'] = ${param.name}.map(String);
    <#else>
        headers['${param.parameterName}'] = ${param.name}.toString();
    </#if>
</#macro>
<#macro setQueryParam param>
<#-- @ftlvariable name="param" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethodParameter" -->
    <#if param.type.name == "string" || param.type.name == "number" || param.type.name == "boolean" || param.type.name == "string[]" || param.type.name == "number[]" || param.type.name == "boolean[]">
        params['${param.parameterName}'] = ${param.name};
    <#else>
        params['${param.parameterName}'] = ${param.name}.toString();
    </#if>
</#macro>
<#macro appendFormData param>
<#-- @ftlvariable name="param" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethodParameter" -->
    <#if param.type.name == 'File'>
        formData.append('${param.parameterName}', ${param.name}, ${param.name}.name);
    <#elseif param.type.name == 'number'>
        formData.append('${param.parameterName}', String(${param.name}));
    <#elseif param.type.name == 'string'>
        formData.append('${param.parameterName}', ${param.name});
    <#else>
        formData.append('${param.parameterName}', ${param.name}.toString());
    </#if>
</#macro>
<#macro url method>
<#-- @ftlvariable name="method" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethod" -->
this.url(${method.basePathParamNames?join(', ')})<#if method.path.path??> + ${method.path.path}</#if></#macro>
<#macro options method>
<#-- @ftlvariable name="method" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethod" -->
<#if method.options??>
<#if method.headerParams?has_content>, {...${method.options}, headers}<#else>, ${method.options}</#if><#else><#if method.headerParams?has_content && method.queryParams?has_content && method.httpMethod == "GET">, {headers, params}<#elseif method.queryParams?has_content && method.httpMethod == "GET">, {params}<#elseif method.headerParams?has_content>, {headers}</#if></#if></#macro>
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {AppConfig} from 'src/app/app-config';
<#list model.dependencies as dependency>
import {${dependency.identifiers?join(", ")}} from '${dependency.path}';
</#list>

/**
 * Autogenerated service client for the java class ${model.className}
 */
@Injectable({
    providedIn: 'root'
})
export class ${model.name} {

    /**
     * Autogenerated constructor for a ${model.name}
     *
     * @param {HttpClient} http the angular http client to perform the requests
     * @param {AppConfig} config the application config to access the backend url
     */
    constructor(private readonly http: HttpClient,
                private readonly config: AppConfig) {
    }

    private url(${model.basePaths[0].params?map(baseUrlParam -> baseUrlParam + ": string | number")?join(", ")}): string {
        return this.config.getBackendUrl()<#if model.basePaths[0].path?has_content> + ${model.basePaths[0].path}</#if>;
    }
    <#list model.methods as method>

    ${method.name}(${method.parameters?map(param -> param.name + ": " + param.type.name + param.optional?then(" | undefined", ""))?join(", ")}): ${method.returnTypeWrapper}<${method.returnType.name}> {
        <#if method.headerParams?has_content>
        const headers: { [header: string]: string | string[] } = {};
            <#list method.headerParams as headerParam>
                <#if headerParam.optional>
                    if(typeof ${headerParam.name} !== 'undefined') {
                    <@setHeaderParam param=headerParam></@setHeaderParam>
                    }
                <#else>
                    <@setHeaderParam param=headerParam></@setHeaderParam>
                </#if>
            </#list>

        </#if>
        <#if method.formData>
        const formData = new FormData();
            <#list method.queryParams as queryParam>
                <#if queryParam.optional>
                    if(typeof ${queryParam.name} !== 'undefined') {
                    <@appendFormData param=queryParam></@appendFormData>
                    }
                <#else>
                    <@appendFormData param=queryParam></@appendFormData>
                </#if>
            </#list>

        <#elseif method.queryParams?has_content && method.httpMethod == "GET">
        const params: { [param: string]: string | number | boolean | (string | number | boolean)[] } = {};
            <#list method.queryParams as param>
                <#if param.optional>
                    if (typeof ${param.name} !== 'undefined') {
                    <@setQueryParam param=param></@setQueryParam>
                    }
                <#else>
                    <@setQueryParam param=param></@setQueryParam>
                </#if>
            </#list>

        </#if>
        <#if method.returnTypeWrapper == 'Promise'>
        return firstValueFrom(this.http.${method.httpMethod?lower_case}<#if method.returnType.name != 'string'><${method.returnType.name}></#if>(<@url method=method></@url><#if method.bodyParameter??>, ${method.bodyParameter}</#if><@options method=method></@options>));
        <#elseif method.returnTypeWrapper == 'Observable'>
        return this.http.${method.httpMethod?lower_case}(<@url method=method></@url><#if method.bodyParameter??>, ${method.bodyParameter}</#if><@options method=method></@options>).pipe(${method.returnTypeMapper});
        <#elseif method.returnTypeWrapper == 'ServerSendEventSource'>
        return new ServerSendEventSource(<@url method=method></@url>);
        </#if>
    }
    </#list>
}
