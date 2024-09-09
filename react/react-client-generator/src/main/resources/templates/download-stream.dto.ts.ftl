<#ftl output_format="JavaScript">
interface DownloadStreamBaseDTO {
cancel: () => Promise
<void>;
    receivedBytes: number;
    expectedBytes?: number;

    }

    export interface DownloadDoneDTO
    <T> extends DownloadStreamBaseDTO {
        done: true;
        value: T
        }

        export interface DownloadProgressDTO extends DownloadStreamBaseDTO {
        done: false;
        }

        export type DownloadStreamDTO
        <T> = DownloadDoneDTO
            <T> | DownloadProgressDTO;
