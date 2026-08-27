import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ComparisonRequest,
  ComparisonSummary,
  DatasetColumns,
  MismatchDetail,
  MissingDetail,
  PagedResult,
  UploadConfigRequest,
  UploadDatasetOptions,
  UploadResponse
} from '../models/comparison.model';

@Injectable({
  providedIn: 'root'
})
export class ComparisonService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/comparisons';

  upload(options: UploadDatasetOptions): Observable<UploadResponse>;
  upload(
    ds1File: File,
    ds2File: File,
    ds1Delimiter?: string,
    ds2Delimiter?: string
  ): Observable<UploadResponse>;
  upload(
    firstArg: File | UploadDatasetOptions,
    ds2File?: File | null,
    ds1Delimiter?: string,
    ds2Delimiter?: string
  ): Observable<UploadResponse> {
    let options: UploadDatasetOptions;

    if (firstArg instanceof File || ds2File !== undefined) {
      options = {
        ds1File: firstArg instanceof File ? firstArg : null,
        ds2File: ds2File || null,
        ds1Delimiter,
        ds2Delimiter
      };
    } else {
      options = firstArg as UploadDatasetOptions;
    }

    const hasFiles = !!options.ds1File || !!options.ds2File;

    if (!hasFiles) {
      const config: UploadConfigRequest = {
        ds1Delimiter: options.ds1Delimiter,
        ds2Delimiter: options.ds2Delimiter,
        ds1Sql: options.ds1Sql,
        ds1Connection: options.ds1Connection,
        ds2Sql: options.ds2Sql,
        ds2Connection: options.ds2Connection
      };
      return this.http.post<UploadResponse>(`${this.baseUrl}/upload`, config);
    }

    const formData = new FormData();
    if (options.ds1File) {
      formData.append('ds1File', options.ds1File);
    }
    if (options.ds2File) {
      formData.append('ds2File', options.ds2File);
    }

    const config: UploadConfigRequest = {
      ds1Delimiter: options.ds1Delimiter,
      ds2Delimiter: options.ds2Delimiter,
      ds1Sql: options.ds1Sql,
      ds1Connection: options.ds1Connection,
      ds2Sql: options.ds2Sql,
      ds2Connection: options.ds2Connection
    };

    const configBlob = new Blob([JSON.stringify(config)], { type: 'application/json' });
    formData.append('config', configBlob);

    let params = new HttpParams();
    if (options.ds1Delimiter && options.ds1Delimiter !== 'AUTO') {
      params = params.set('ds1Delimiter', options.ds1Delimiter);
    }
    if (options.ds2Delimiter && options.ds2Delimiter !== 'AUTO') {
      params = params.set('ds2Delimiter', options.ds2Delimiter);
    }
    if (options.ds1Sql) {
      params = params.set('ds1Sql', options.ds1Sql);
    }
    if (options.ds2Sql) {
      params = params.set('ds2Sql', options.ds2Sql);
    }

    return this.http.post<UploadResponse>(`${this.baseUrl}/upload`, formData, { params });
  }

  execute(id: string, request: ComparisonRequest): Observable<ComparisonSummary> {
    return this.http.post<ComparisonSummary>(`${this.baseUrl}/${id}/execute`, request);
  }

  getComparison(id: string): Observable<ComparisonSummary> {
    return this.http.get<ComparisonSummary>(`${this.baseUrl}/${id}`);
  }

  listComparisons(): Observable<ComparisonSummary[]> {
    return this.http.get<ComparisonSummary[]>(this.baseUrl);
  }

  getHeaders(id: string): Observable<DatasetColumns> {
    return this.http.get<DatasetColumns>(`${this.baseUrl}/${id}/headers`);
  }

  getMismatches(
    id: string,
    page = 0,
    size = 50,
    direction?: string
  ): Observable<PagedResult<MismatchDetail>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (direction) {
      params = params.set('direction', direction);
    }
    return this.http.get<PagedResult<MismatchDetail>>(`${this.baseUrl}/${id}/mismatches`, { params });
  }

  getMissing(
    id: string,
    page = 0,
    size = 50,
    direction?: string
  ): Observable<PagedResult<MissingDetail>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (direction) {
      params = params.set('direction', direction);
    }
    return this.http.get<PagedResult<MissingDetail>>(`${this.baseUrl}/${id}/missing`, { params });
  }

  getMatches(id: string, page = 0, size = 50): Observable<PagedResult<Record<string, unknown>>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PagedResult<Record<string, unknown>>>(`${this.baseUrl}/${id}/matches`, { params });
  }

  deleteComparison(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getReportUrl(id: string): string {
    return `${this.baseUrl}/${id}/report`;
  }
}
