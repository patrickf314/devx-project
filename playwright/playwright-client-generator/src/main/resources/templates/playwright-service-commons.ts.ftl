<#ftl output_format="JavaScript">
import { APIResponse } from '@playwright/test';

export function url(pathname: string, searchParams: Record
<string, string | number | boolean | undefined | null | {
toString: () => string
}> = {}): string {
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

return result.toString();
}

export async function mapJsonResponse
<T>(res: APIResponse): Promise
    <T> {
        if (res.status() !== 200) {
        throw new Error('http error response: ' + res.status());
        }

        const contentType = res.headers()['content-type'];
        if (contentType !== 'application/json') {
        throw invalidResponseBodyError(contentType);
        }

        return await res.json() as T;
        }

        export async function mapVoidResponse(res: APIResponse): Promise
        <void> {
            if (res.status() !== 200) {
            throw new Error('http error response: ' + res.status());
            }
            }

            export async function mapStringResponse(res: APIResponse): Promise
            <string> {
                if (res.status() !== 200) {
                throw new Error('http error response: ' + res.status());
                }

                const contentType = res.headers()['content-type'];
                if (contentType === null || (contentType !== 'text/plain' && !contentType.startsWith('text/plain;'))) {
                throw invalidResponseBodyError(contentType);
                }

                return await res.text();
                }

                export async function mapStreamingResponse(res: APIResponse): Promise
                <Buffer> {
                    if (res.status() !== 200) {
                    throw new Error('http error response: ' + res.status());
                    }

                    return res.body();
                    }

                    function invalidResponseBodyError(actualContentType: string | null): Error {
                    return new Error(`Invalid response body: contentType is ${r"${actualContentType ?? 'null'}"}`);
                    }