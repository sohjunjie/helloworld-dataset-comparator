import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { httpErrorInterceptor } from './http-error.interceptor';

describe('HttpErrorInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let snackBar: MatSnackBar;
  let snackBarOpenSpy: any;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MatSnackBarModule],
      providers: [
        provideHttpClient(withInterceptors([httpErrorInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    snackBar = TestBed.inject(MatSnackBar);
    snackBarOpenSpy = vi.spyOn(snackBar, 'open').mockImplementation(() => ({} as any));
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should display snackbar toast with default message for 413 File Too Large', () => {
    httpClient.post('/api/v1/comparisons/upload', {}).subscribe({
      next: () => {
        throw new Error('should have failed with 413');
      },
      error: (err: HttpErrorResponse) => {
        expect(err.status).toBe(413);
      }
    });

    const req = httpTestingController.expectOne('/api/v1/comparisons/upload');
    req.flush('Payload Too Large', { status: 413, statusText: 'Payload Too Large' });

    expect(snackBarOpenSpy).toHaveBeenCalledWith(
      'File too large. Maximum allowed size is 500MB.',
      'Dismiss',
      expect.objectContaining({
        duration: 5000,
        panelClass: ['error-snackbar']
      })
    );
  });

  it('should display response body message for 413 if custom message provided', () => {
    httpClient.post('/api/v1/comparisons/upload', {}).subscribe({
      next: () => {
        throw new Error('should have failed');
      },
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/v1/comparisons/upload');
    req.flush({ message: 'Dataset 1 exceeds 500MB limit.' }, { status: 413, statusText: 'Payload Too Large' });

    expect(snackBarOpenSpy).toHaveBeenCalledWith(
      'Dataset 1 exceeds 500MB limit.',
      'Dismiss',
      expect.objectContaining({ duration: 5000 })
    );
  });

  it('should display connection failure toast for status 0 (backend not running)', () => {
    httpClient.get('/api/v1/comparisons').subscribe({
      next: () => {
        throw new Error('should have failed with 0');
      },
      error: (err: HttpErrorResponse) => {
        expect(err.status).toBe(0);
      }
    });

    const req = httpTestingController.expectOne('/api/v1/comparisons');
    req.error(new ProgressEvent('error'), { status: 0, statusText: 'Unknown Error' });

    expect(snackBarOpenSpy).toHaveBeenCalledWith(
      'Unable to connect to server. Please check your connection and ensure the backend is running.',
      'Dismiss',
      expect.objectContaining({
        duration: 5000,
        panelClass: ['error-snackbar']
      })
    );
  });

  it('should display server error message from body for 400 Bad Request (e.g. SQL execution failed)', () => {
    httpClient.post('/api/v1/comparisons/comp-1/execute', {}).subscribe({
      next: () => {
        throw new Error('should have failed with 400');
      },
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/v1/comparisons/comp-1/execute');
    req.flush(
      { message: 'SQL execution failed: relation "employees" does not exist' },
      { status: 400, statusText: 'Bad Request' }
    );

    expect(snackBarOpenSpy).toHaveBeenCalledWith(
      'SQL execution failed: relation "employees" does not exist',
      'Dismiss',
      expect.objectContaining({ duration: 5000 })
    );
  });

  it('should display comparison timeout message for 408 / 504 status', () => {
    httpClient.post('/api/v1/comparisons/comp-1/execute', {}).subscribe({
      next: () => {
        throw new Error('should have failed with 504');
      },
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/v1/comparisons/comp-1/execute');
    req.flush('Gateway Timeout', { status: 504, statusText: 'Gateway Timeout' });

    expect(snackBarOpenSpy).toHaveBeenCalledWith(
      'Comparison request timed out. Please check your data size or database query.',
      'Dismiss',
      expect.objectContaining({ duration: 5000 })
    );
  });

  it('should display unexpected server error for 500 without custom body message', () => {
    httpClient.get('/api/v1/comparisons/comp-1').subscribe({
      next: () => {
        throw new Error('should have failed with 500');
      },
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/v1/comparisons/comp-1');
    req.flush(null, { status: 500, statusText: 'Internal Server Error' });

    expect(snackBarOpenSpy).toHaveBeenCalledWith(
      'Unexpected server error occurred.',
      'Dismiss',
      expect.objectContaining({ duration: 5000 })
    );
  });

  it('should display server error message from body for 500 with custom body message', () => {
    httpClient.get('/api/v1/comparisons/comp-1').subscribe({
      next: () => {
        throw new Error('should have failed with 500');
      },
      error: () => {}
    });

    const req = httpTestingController.expectOne('/api/v1/comparisons/comp-1');
    req.flush({ message: 'DuckDB out of memory during comparison execution' }, { status: 500, statusText: 'Internal Server Error' });

    expect(snackBarOpenSpy).toHaveBeenCalledWith(
      'DuckDB out of memory during comparison execution',
      'Dismiss',
      expect.objectContaining({ duration: 5000 })
    );
  });
});

