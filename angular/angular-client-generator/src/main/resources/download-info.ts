import { map } from 'rxjs/operators';
import { HttpEvent, HttpEventType, HttpHeaders, HttpParams } from '@angular/common/http';

export type DownloadInfo = DownloadInfoPending | DownloadInfoProgress | DownloadInfoFinished | DownloadInfoFailed;

export type HttpDownloadOptions = {
    headers?: HttpHeaders | {
        [header: string]: string | string[];
    };
    observe: 'events';
    params?: HttpParams | {
        [param: string]: string | string[];
    };
    reportProgress?: boolean;
    responseType: 'blob';
    withCredentials?: boolean;
};

export const HTTP_DOWNLOAD_OPTIONS: HttpDownloadOptions = {
    reportProgress: true,
    observe: 'events',
    responseType: 'blob'
};

export const HTTP_DOWNLOAD_PIPE = map((event: HttpEvent<Blob>): DownloadInfo => {
    if (event.type === HttpEventType.DownloadProgress || event.type === HttpEventType.UploadProgress) {
        return { progress: event.total ? event.loaded * 100 / event.total : event.loaded, state: 'IN_PROGRESS' };
    }

    if (event.type === HttpEventType.Response) {
        if (event.status === 200 && event.body) {
            return { state: 'DONE', content: event.body };
        } else {
            return { state: 'FAILED', status: event.status };
        }
    }

    return { state: 'PENDING' };
});

export interface DownloadInfoPending {
    state: 'PENDING';
}

export interface DownloadInfoProgress {
    state: 'IN_PROGRESS',
    progress: number;
}

export interface DownloadInfoFinished {
    state: 'DONE';
    content: Blob
}

export interface DownloadInfoFailed {
    state: 'FAILED';
    status: number;
}
