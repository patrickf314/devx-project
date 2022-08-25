import {Observable, Subscriber} from 'rxjs';

export interface ServerSentEvent {

    id?: string;
    data?: string;
    timeStamp: number;

}

export class ServerSendEventSource<T> extends Observable<T> {

    private readonly eventSource: EventSource;
    private subscribers: Subscriber<T>[] = [];

    constructor(url: string) {
        super(subscriber => {
            this.subscribers.push(subscriber);
        });

        this.eventSource = new EventSource(url, {withCredentials: true});

        this.eventSource.onmessage = message => {
            this.subscribers.forEach(subscriber => subscriber.next({
                id: message.lastEventId,
                data: message.data,
                time: message.timeStamp
            }));
        };

        this.eventSource.onerror = error => this.subscribers.forEach(subscriber => subscriber.error(error));
    }

    close(): void {
        this.subscribers.forEach(subscriber => subscriber.complete());
        this.subscribers = [];

        this.eventSource.close();
    }
}
