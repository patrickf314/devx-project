<#ftl output_format="JavaScript">
<#-- -->
/* eslint-disable @typescript-eslint/consistent-type-imports */
// noinspection DuplicatedCode

<#-- @ftlvariable name="model" type="api.maven.plugin.angular.client.data.TypeScriptService" -->
<#macro setHeaderParam param>
<#-- @ftlvariable name="param" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethodParameter" -->
<#if param.type.name == "string" || param.type.name == "string[]" >headers.set('${param.parameterName}', ${param.name});
<#elseif param.type.name == "number" || param.type.name == "boolean">headers.set('${param.parameterName}', String(${param.name}));
<#elseif param.type.name == "number[]"  || param.type.name == "boolean[]" >headers.set('${param.parameterName}', ${param.name}.map(String).join(', '));
<#else>headers.set('${param.parameterName}', ${param.name}.toString());
</#if>
</#macro>
<#-- -->
<#-- -->
<#macro setQueryParam param>
<#-- @ftlvariable name="param" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethodParameter" -->
<#if param.type.name == "string" || param.type.name == "number" || param.type.name == "boolean" || param.type.name == "string[]" || param.type.name == "number[]" || param.type.name == "boolean[]">params.${param.parameterName} = ${param.name};
<#else>params.${param.parameterName} = ${param.name}.toString();
</#if>
</#macro>
<#-- -->
<#-- -->
<#macro appendFormData param>
<#-- @ftlvariable name="param" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethodParameter" -->
<#if param.type.name == 'File'>formData.append('${param.parameterName}', ${param.name}, ${param.name}.name);
<#elseif param.type.name == 'number'>formData.append('${param.parameterName}', String(${param.name}));
<#elseif param.type.name == 'string'>formData.append('${param.parameterName}', ${param.name});
<#else>formData.append('${param.parameterName}', ${param.name}.toString());
</#if>
</#macro>
<#-- -->
<#-- -->
<#macro url method state>
<#-- @ftlvariable name="method" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethod" -->
<#-- @ftlvariable name="state" type="java.lang.String" -->
<#if method.path.path?has_content>${r"`${baseUrl(" + state + method.basePathParamNames?has_content?then(", ", "") + method.basePathParamNames?join(', ') + ")}" + method.path.path + "`"}<#else>baseUrl(${state}${method.basePathParamNames?has_content?then(", ", "")}${method.basePathParamNames?join(', ')})</#if></#macro>
<#-- -->
<#-- -->
<#macro methodParameterType method emptyParameterType>
<#-- @ftlvariable name="method" type="api.maven.plugin.angular.client.data.TypeScriptServiceMethod" -->
<#-- @ftlvariable name="emptyParameterType" type="java.lang.String" -->
<#if method.parameters?has_content>{ ${method.parameters?map(param -> param.name + param.optional?then("?", "") + ": " + param.type.name + param.optional?then(" | undefined", ""))?join(", ")} }<#else>${emptyParameterType}</#if></#macro>
<#-- -->
<#-- -->
<#list model.dependencies as dependency>
import { ${dependency.identifiers?join(", ")} } from '${dependency.path}';
</#list>

function baseUrl(state: AppState${model.basePaths[0].params?has_content?then(", ", "")}${model.basePaths[0].params?map(baseUrlParam -> baseUrlParam + ": string | number")?join(", ")}): string {
    return <#if model.basePaths[0].path?has_content>${r"`${backendUrl(state)}" + model.basePaths[0].path + "`"}<#else>backendUrl(state)</#if>;
}
<#list model.methods as method>

export const ${method.name}Thunk = createAsyncThunk<<#if method.returnType.name == "void">undefined<#elseif method.returnType.name == 'DownloadInfo'>DownloadStreamDTO<Uint8Array><#else>${method.returnType.name}</#if>, <@methodParameterType method=method emptyParameterType="undefined"/>>('${model.name}/${method.name}', async function(arg: <@methodParameterType method=method emptyParameterType="undefined"/>, thunkAPI): <#if method.returnTypeWrapper == 'Observable'>Promise<DownloadStreamDTO<Uint8Array>><#else>${method.returnTypeWrapper}<<#if method.returnType.name == "void">undefined<#else>${method.returnType.name}</#if>></#if> {
    <#if method.parameters?has_content>
    const { ${method.parameters?map(param -> param.name)?join(", ")} } = arg;
    </#if>

    const headers = prepareHeaders(thunkAPI.getState() as AppState<#if method.bodyParameter?has_content>, { contentType: 'json' }<#elseif method.formData>, { contentType: 'formData' }</#if>);
    <#if method.headerParams?has_content>
    <#list method.headerParams as headerParam>
    <#if headerParam.optional>if (typeof ${headerParam.name} !== 'undefined') {
        <@setHeaderParam param=headerParam></@setHeaderParam>    }
    <#else><@setHeaderParam param=headerParam></@setHeaderParam>
    </#if>
    </#list>
    </#if>

    <#if method.formData>
    const formData = new FormData();
    <#list method.queryParams as queryParam>
    <#if queryParam.optional>if (typeof ${queryParam.name} !== 'undefined') {
        <@appendFormData param=queryParam></@appendFormData>    }
    <#else><@appendFormData param=queryParam></@appendFormData>
    </#if>
    </#list>

    <#elseif method.queryParams?has_content && method.httpMethod == "GET">
    const params: Record<string, string | number | boolean | Array<string | number | boolean>> = {};
    <#list method.queryParams as param>
    <#if param.optional>if (typeof ${param.name} !== 'undefined') {
        <@setQueryParam param=param></@setQueryParam>    }
    <#else><@setQueryParam param=param></@setQueryParam>
    </#if>
    </#list>

    </#if>
    <#if method.returnType.name != "void">return </#if>await fetch(url(<@url method=method state="thunkAPI.getState() as AppState"></@url><#if method.queryParams?has_content && method.httpMethod == "GET">, params</#if>), {
        method: '${method.httpMethod?lower_case}',
        credentials: 'include',
        headers<#if method.formData || method.bodyParameter?has_content>,
        body: <#if method.bodyParameter?has_content>JSON.stringify(${method.bodyParameter})<#else>formData</#if>
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
    const dispatch = useAppDispatch();

    return useMemo(() => {
        return {
            ${model.methods?map(method -> method.name + ": (" + method.parameters?has_content?then("args: { " + method.parameters?map(param -> param.name + param.optional?then("?", "") + ": " + param.type.name + param.optional?then(" | undefined", ""))?join(", ")  + " }", "") + ") => dispatch(" + method.name + "Thunk(" + method.parameters?has_content?then("args","" ) + ")).unwrap()")?join(',\n            ')}
        };
    }, [dispatch]);
}
