<#ftl output_format="JavaScript">
<#-- @ftlvariable name="imports" type="java.util.List<de.devx.project.commons.client.typescript.data.TypeScriptImportModel>" -->
<#-- @ftlvariable name="errorMapperIdentifier" type="java.lang.String" -->
<#-- @ftlvariable name="generateZodSchemas" type="java.lang.Boolean" -->
<#list imports as import>
import { ${import.identifiers?join(", ")} } from '${import.path}';
</#list>

export function url(pathname: string, searchParams: Record<string, string | number | boolean | undefined | null | {
    toString: () => string
}> = {}): URL {
    const result = new URL(pathname);

    for (const param of Object.keys(searchParams)) {
        const value = searchParams[param];
        if (typeof value === 'undefined' || value === null) {
            continue;
        }

        if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
            result.searchParams.append(param, String(value));
        } else {
            result.searchParams.append(param, value.toString());
        }
    }

    return result;
}

export async function mapJsonResponse<T>(res: Response<#if generateZodSchemas>, schema?: { parse: (data: unknown) => T }</#if>): Promise<T> {
    if (res.status !== 200) {
        throw await ${errorMapperIdentifier}(res);
    }

    const contentType = res.headers.get('Content-Type');
    if (contentType !== 'application/json') {
        throw invalidResponseBodyError(contentType);
    }

    <#if generateZodSchemas>const data = await res.json();
    return schema ? schema.parse(data) : data as T;<#else>return await res.json() as T;</#if>
}

export async function mapVoidResponse(res: Response): Promise<void> {
    if (res.status !== 200) {
        throw await ${errorMapperIdentifier}(res);
    }
}

export async function mapStringResponse(res: Response): Promise<string> {
    if (res.status !== 200) {
        throw await ${errorMapperIdentifier}(res);
    }

    const contentType = res.headers.get('Content-Type');
    if (contentType === null || (contentType !== 'text/plain' && !contentType.startsWith('text/plain;'))) {
        throw invalidResponseBodyError(contentType);
    }

    return await res.text();
}

export async function mapBlobResponse(res: Response): Promise<Blob> {
    if (res.status !== 200) {
        throw await ${errorMapperIdentifier}(res);
    }

    return await res.blob();
}

function invalidResponseBodyError(actualContentType: string | null): Error {
    return new Error(`Invalid response body: contentType is ${r"${actualContentType ?? 'null'}"}`);
}
