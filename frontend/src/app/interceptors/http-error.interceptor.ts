import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const errorMessage = extractErrorMessage(error);

      snackBar.open(errorMessage, 'Dismiss', {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });

      return throwError(() => error);
    })
  );
};

function extractErrorMessage(error: HttpErrorResponse): string {
  // If response body has a structured message
  if (error.error && typeof error.error === 'object' && error.error.message) {
    return error.error.message;
  }

  if (error.status === 413) {
    return 'File too large. Maximum allowed size is 500MB.';
  }

  if (error.status === 0) {
    return 'Unable to connect to server. Please check your connection and ensure the backend is running.';
  }

  if (error.status === 408 || error.status === 504) {
    return 'Comparison request timed out. Please check your data size or database query.';
  }

  if (typeof error.error === 'string' && error.error.trim().length > 0 && !error.error.trim().startsWith('<')) {
    return error.error.trim();
  }

  if (error.status >= 500) {
    return 'Unexpected server error occurred.';
  }

  if (error.status >= 400) {
    return error.statusText || 'Request failed.';
  }

  return error.message || 'An unexpected error occurred while communicating with the server.';
}
