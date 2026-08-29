import '../../test-setup';
import { TestBed } from '@angular/core/testing';
import { ProgressService } from './progress.service';
import { ProgressUpdate } from '../models/comparison.model';

describe('ProgressService', () => {
  let service: ProgressService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProgressService]
    });
    service = TestBed.inject(ProgressService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should subscribe to SSE stream and emit parsed progress events', async () => {
    const comparisonId = 'comp-123';
    const mockUpdate: ProgressUpdate = {
      stage: 'COMPARING',
      percent: 50,
      message: 'Processing comparisons'
    };

    let eventSourceInstance: any;
    const originalEventSource = window.EventSource;
    (window as any).EventSource = function (url: string) {
      this.url = url;
      this.close = () => {};
      eventSourceInstance = this;
    };

    const promise = new Promise<ProgressUpdate>((resolve) => {
      const sub = service.subscribe(comparisonId).subscribe({
        next: (update) => {
          sub.unsubscribe();
          (window as any).EventSource = originalEventSource;
          resolve(update);
        }
      });
    });

    // Simulate SSE message event
    setTimeout(() => {
      if (eventSourceInstance?.onmessage) {
        eventSourceInstance.onmessage({ data: JSON.stringify(mockUpdate) });
      }
    }, 10);

    const result = await promise;
    expect(result.stage).toBe('COMPARING');
    expect(result.percent).toBe(50);
  });
});
