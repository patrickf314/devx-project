<#ftl output_format="JavaScript">
<#-- @ftlvariable name="model" type="de.devx.project.commons.client.typescript.data.TypeScriptServiceModel" -->
<#-- @ftlvariable name="imports" type="java.util.Collection<de.devx.project.commons.client.typescript.data.TypeScriptImportModel>" -->
<#-- @ftlvariable name="prepareHeadersIdentifier" type="java.lang.String" -->
<#-- @ftlvariable name="backendUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backendUrlGetterIdentifier" type="java.lang.String" -->
<#-- -->
<#macro setHeaderParam param>
<#-- @ftlvariable name="param" type="de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodParameterModel" -->
<#if param.type.name == "string" || param.type.name == "string[]" >headers.set('${param.parameterName}', ${param.name});
<#elseif param.type.name == "number" || param.type.name == "boolean">headers.set('${param.parameterName}', String(${param.name}));
<#elseif param.type.name == "number[]"  || param.type.name == "boolean[]" >headers.set('${param.parameterName}', ${param.name}.map(String).join(', '));
<#else>headers.set('${param.parameterName}', ${param.name}.toString());
</#if>
</#macro>
<#-- -->
<#-- -->
<#macro setQueryParam param>
<#-- @ftlvariable name="param" type="de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodParameterModel" -->
<#if param.type.name == "string" || param.type.name == "number" || param.type.name == "boolean" || param.type.name == "string[]" || param.type.name == "number[]" || param.type.name == "boolean[]">params.${param.parameterName} = ${param.name};
<#else>params.${param.parameterName} = ${param.name}.toString();
</#if>
</#macro>
<#-- -->
<#-- -->
<#macro appendFormData param>
<#-- @ftlvariable name="param" type="de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodParameterModel" -->
<#if param.type.name == 'File'>formData.append('${param.parameterName}', ${param.name}, ${param.name}.name);
<#elseif param.type.name == 'number'>formData.append('${param.parameterName}', String(${param.name}));
<#elseif param.type.name == 'string'>formData.append('${param.parameterName}', ${param.name});
<#else>formData.append('${param.parameterName}', ${param.name}.toString());
</#if>
</#macro>
<#-- -->
<#-- -->
<#macro url method state>
<#-- @ftlvariable name="method" type="de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodModel" -->
<#-- @ftlvariable name="state" type="java.lang.String" -->
<#if method.path.path?has_content>${r"`${baseUrl(" + state + method.basePathParamNames?has_content?then(", ", "") + method.basePathParamNames?join(', ') + ")}" + method.path.path + "`"}<#else>baseUrl(${state}${method.basePathParamNames?has_content?then(", ", "")}${method.basePathParamNames?join(', ')})</#if></#macro>
<#-- -->
<#-- -->
<#macro methodParameterType method emptyParameterType>
<#-- @ftlvariable name="method" type="de.devx.project.commons.client.typescript.data.TypeScriptServiceMethodModel" -->
<#-- @ftlvariable name="emptyParameterType" type="java.lang.String" -->
<#if method.parameters?has_content>{ ${method.parameters?map(param -> param.name + param.optional?then("?", "") + ": " + param.type.name + param.optional?then(" | undefined", ""))?join(", ")} }<#else>${emptyParameterType}</#if></#macro>
<#-- -->
<#-- -->
import { createAsyncThunk } from '@reduxjs/toolkit';
import { useDispatch } from 'react-redux';
import { useMemo } from 'react';
<#list imports as import>
import { ${import.identifiers?join(", ")} } from '${import.path}';
</#list>

function baseUrl(state: State${model.basePaths[0].params?has_content?then(", ", "")}${model.basePaths[0].params?map(baseUrlParam -> baseUrlParam + ": string | number")?join(", ")}): string {
    return <#if model.basePaths[0].path?has_content>${"`" + backendUrl?has_content?then(backendUrl, r"${" + backendUrlGetterIdentifier + "(state)}") + model.basePaths[0].path + "`"}<#else>backendUrl(state)</#if>;
}
<#list model.methods as method>

export const ${method.name}Thunk = createAsyncThunk('${model.name}/${method.name}', async function(arg: <@methodParameterType method=method emptyParameterType="undefined"/>, thunkAPI): <#if method.returnTypeWrapper == 'Observable'>Promise<DownloadStreamDTO<Uint8Array>><#else>${method.returnTypeWrapper}<${method.returnType.name}></#if> {
    <#if method.parameters?has_content>
    const { ${method.parameters?map(param -> param.name)?join(", ")} } = arg;
    </#if>

    const headers = new Headers();
    <#if method.bodyParameter?has_content && !method.formData>
    headers.set('Content-Type', 'application/json');
    <#elseif method.formData>
    headers.set('Content-Type', 'application/x-www-form-urlencoded');
    </#if>
    <#if method.headerParams?has_content>
    <#list method.headerParams as headerParam>
    <#if headerParam.optional>if (typeof ${headerParam.name} !== 'undefined') {
        <@setHeaderParam param=headerParam></@setHeaderParam>    }
    <#else><@setHeaderParam param=headerParam></@setHeaderParam>
    </#if>
    </#list>
    </#if>
    <#if prepareHeadersIdentifier?has_content>${prepareHeadersIdentifier}(headers, thunkAPI.getState());</#if>

    <#if method.formData>
    const formData = new FormData();
    <#list method.queryParams as queryParam>
    <#if queryParam.optional>if (typeof ${queryParam.name} !== 'undefined') {
        <@appendFormData param=queryParam></@appendFormData>    }
    <#else><@appendFormData param=queryParam></@appendFormData>
    </#if>
    </#list>

    <#elseif method.queryParams?has_content && (method.httpMethod == "GET" || method.httpMethod == "DELETE")>
    const params: Record<string, string | number | boolean | Array<string | number | boolean>> = {};
    <#list method.queryParams as param>
    <#if param.optional>if (typeof ${param.name} !== 'undefined') {
        <@setQueryParam param=param></@setQueryParam>    }
    <#else><@setQueryParam param=param></@setQueryParam>
    </#if>
    </#list>

    </#if>
    <#if method.returnType.name != "void">return </#if>await fetch(url(<@url method=method state="thunkAPI.getState()"></@url><#if method.queryParams?has_content && (method.httpMethod == "GET" || method.httpMethod == "DELETE")>, params</#if>), {
        method: '${method.httpMethod?lower_case}',
        credentials: 'include',
        headers<#if method.formData || method.bodyParameter?has_content>,
        body: <#if method.bodyParameter?has_content && !method.formData>JSON.stringify(${method.bodyParameter})<#else>formData</#if>
        </#if>

    }).then(res => map<#switch method.returnType.name><#case 'void'>VoidResponse<#break><#case 'string'>StringResponse<#break><#case 'DownloadInfo'>StreamingResponse<#break><#default>JsonResponse<${method.returnType.name}></#switch>(res));
}, ThunkOptions);
</#list>

/**
 * Autogenerated service interface for the java class ${model.className}
 */
export interface ${model.name} {
<#list model.methods as method>
    ${method.name}: (${method.parameters?has_content?then('args: ', '')}<@methodParameterType method=method emptyParameterType=""/>) => Promise<${(method.returnType.name == "DownloadInfo")?then("DownloadStreamDTO<Uint8Array>", method.returnType.name)}>;
</#list>
}

/**
 * Autogenerated service client for the java class ${model.className}
 */
export function use${model.name}(): ${model.name} {
    const dispatch: Dispatch = useDispatch();

    return useMemo(() => {
        return {
            ${model.methods?map(method -> method.name + ": (" + method.parameters?has_content?then("args: { " + method.parameters?map(param -> param.name + param.optional?then("?", "") + ": " + param.type.name + param.optional?then(" | undefined", ""))?join(", ")  + " }", "") + ") => dispatch(" + method.name + "Thunk(" + method.parameters?has_content?then("args","" ) + ")).unwrap()")?join(',\n            ')}
        };
    }, [dispatch]);
}
