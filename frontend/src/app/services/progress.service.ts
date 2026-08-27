import { Injectable, NgZone, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ProgressUpdate } from '../models/comparison.model';

@Injectable({
  providedIn: 'root'
})
export class ProgressService {
  private readonly zone = inject(NgZone);

  subscribe(comparisonId: string): Observable<ProgressUpdate> {
    return new Observable<ProgressUpdate>((subscriber) => {
      const url = `/api/v1/comparisons/${comparisonId}/events`;
      const eventSource = new EventSource(url);

      eventSource.onmessage = (event) => {
        this.zone.run(() => {
          try {
            const data: ProgressUpdate = JSON.parse(event.data);
            subscriber.next(data);

            if (data.stage === 'COMPLETED') {
              eventSource.close();
              subscriber.complete();
            } else if (data.stage === 'FAILED') {
              eventSource.close();
              subscriber.error(new Error(data.message || 'Comparison failed'));
            }
          } catch (e) {
            subscriber.error(e);
          }
        });
      };

      eventSource.onerror = (error) => {
        this.zone.run(() => {
          // If the stream is closed by server on completion
          if (eventSource.readyState === EventSource.CLOSED) {
            subscriber.complete();
          } else {
            eventSource.close();
            subscriber.error(error);
          }
        });
      };

      return () => {
        eventSource.close();
      };
    });
  }
}
