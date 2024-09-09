<#ftl output_format="JavaScript">
<#-- @ftlvariable name="imports" type="java.util.List<de.devx.project.commons.client.typescript.data.TypeScriptImportModel>" -->
<#-- @ftlvariable name="errorMapperIdentifier" type="java.lang.String" -->
import { type DownloadStreamDTO } from './download-stream.dto';
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

export async function mapJsonResponse<T>(res: Response): Promise<T> {
    if (res.status !== 200) {
        throw await ${errorMapperIdentifier}(res);
    }

    const contentType = res.headers.get('Content-Type');
    if (contentType !== 'application/json') {
        throw invalidResponseBodyError(contentType);
    }

    return await res.json() as T;
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

export async function mapStreamingResponse(res: Response): Promise<DownloadStreamDTO<Uint8Array>> {
    if (res.status !== 200) {
        throw await ${errorMapperIdentifier}(res);
    }

    const contentLength = res.headers.get('Content-Length');
    const expectedBytes = contentLength == null ? undefined : Number(contentLength);
    if (res.body === null) {
        throw new Error('Invalid response body: null');
    }

    const reader = res.body.getReader();
    const stream = new DownloadStream(reader, expectedBytes);

    stream.run();

    return stream as DownloadStreamDTO<Uint8Array>;
}

function invalidResponseBodyError(actualContentType: string | null): Error {
    return new Error(`Invalid response body: contentType is ${r"${actualContentType ?? 'null'}"}`);
}

class DownloadStream {

    done = false;
    private pid = -1;
    private canceled = false;
    receivedBytes = 0;
    private readonly chunks = Array<Uint8Array>();
    value: Uint8Array | undefined;

    constructor(private readonly reader: ReadableStreamDefaultReader<Uint8Array>,
                readonly expectedBytes?: number,
                private readonly timeout: number = 100) {

    }

    run(): void {
        if (this.canceled) {
            return;
        }

        this.pid = window.setTimeout(() => {
            this.pid = -1;
            this.readNext().then(() => this.run());
        }, this.timeout);
    }

    private async readNext(): Promise<void> {
        const result = await this.reader.read();

        if (result.done) {
            this.complete();
            return;
        }

        this.chunks.push(result.value);
        this.receivedBytes += result.value.length;
    }

    private complete(): void {
        const value = new Uint8Array(this.receivedBytes);
        let position = 0;
        for (const chunk of this.chunks) {
            value.set(chunk, position);
            position += chunk.length;
        }
        this.value = value;
        this.done = true;
    }

    async cancel(): Promise<void> {
        this.canceled = true;

        if (this.pid !== -1) {
            window.clearTimeout(this.pid);
        }

        await this.reader.cancel();
    }
}
