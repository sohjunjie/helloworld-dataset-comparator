import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComparisonService } from './comparison.service';
import {
  ComparisonRequest,
  ComparisonSummary,
  DatasetColumns,
  PagedResult,
  UploadDatasetOptions,
  UploadResponse
} from '../models/comparison.model';

describe('ComparisonService', () => {
  let service: ComparisonService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ComparisonService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ComparisonService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should upload files with multipart/form-data', () => {
    const file1 = new File(['id,name\n1,Alice'], 'ds1.csv', { type: 'text/csv' });
    const file2 = new File(['id,name\n1,Bob'], 'ds2.csv', { type: 'text/csv' });
    const mockResponse: UploadResponse = {
      comparisonId: 'comp-123',
      columns: { ds1: ['id', 'name'], ds2: ['id', 'name'] }
    };

    service.upload(file1, file2, ',', ',').subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpTesting.expectOne((r) => r.url.endsWith('/api/v1/comparisons/upload') || r.url.endsWith('/api/comparisons/upload'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush(mockResponse);
  });

  it('should upload SQL-only datasets with JSON body', () => {
    const options: UploadDatasetOptions = {
      ds1Sql: 'SELECT id, name FROM staff',
      ds1Connection: { host: 'pg1', port: 5432, database: 'db1', username: 'u1', password: 'p1' },
      ds2Sql: 'SELECT id, name FROM employees',
      ds2Connection: { host: 'pg2', port: 5432, database: 'db2', username: 'u2', password: 'p2' }
    };

    const mockResponse: UploadResponse = {
      comparisonId: 'comp-sql-123',
      columns: { ds1: ['id', 'name'], ds2: ['id', 'name'] }
    };

    service.upload(options).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpTesting.expectOne((r) => r.url.endsWith('/api/v1/comparisons/upload') || r.url.endsWith('/api/comparisons/upload'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      ds1Delimiter: undefined,
      ds2Delimiter: undefined,
      ds1Sql: options.ds1Sql,
      ds1Connection: options.ds1Connection,
      ds2Sql: options.ds2Sql,
      ds2Connection: options.ds2Connection
    });
    req.flush(mockResponse);
  });

  it('should upload mixed datasets (DS1 file + DS2 SQL) with multipart/form-data', () => {
    const file1 = new File(['id,name\n1,Alice'], 'ds1.csv', { type: 'text/csv' });
    const options: UploadDatasetOptions = {
      ds1File: file1,
      ds1Delimiter: ',',
      ds2Sql: 'SELECT id, name FROM employees',
      ds2Connection: { host: 'pg2', port: 5432, database: 'db2', username: 'u2', password: 'p2' }
    };

    const mockResponse: UploadResponse = {
      comparisonId: 'comp-mixed-123',
      columns: { ds1: ['id', 'name'], ds2: ['id', 'name'] }
    };

    service.upload(options).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpTesting.expectOne((r) => r.url.endsWith('/api/v1/comparisons/upload') || r.url.endsWith('/api/comparisons/upload'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush(mockResponse);
  });

  it('should execute a comparison', () => {
    const comparisonId = 'comp-123';
    const request: ComparisonRequest = {
      keyColumns: ['id'],
      tolerances: [{ columnName: 'amount', percentage: 1.0 }],
      caseSensitive: true
    };
    const mockSummary: ComparisonSummary = {
      id: comparisonId,
      status: 'COMPARING',
      createdAt: '2026-08-28T00:00:00Z',
      ds1Type: 'FILE_UPLOAD',
      ds2Type: 'FILE_UPLOAD'
    };

    service.execute(comparisonId, request).subscribe(res => {
      expect(res).toEqual(mockSummary);
    });

    const req = httpTesting.expectOne((r) => r.url.includes(`/api/v1/comparisons/${comparisonId}/execute`) || r.url.includes(`/api/comparisons/${comparisonId}/execute`));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockSummary);
  });

  it('should get comparison by ID', () => {
    const comparisonId = 'comp-123';
    const mockSummary: ComparisonSummary = {
      id: comparisonId,
      status: 'COMPLETED',
      createdAt: '2026-08-28T00:00:00Z',
      ds1Type: 'FILE_UPLOAD',
      ds2Type: 'FILE_UPLOAD'
    };

    service.getComparison(comparisonId).subscribe(res => {
      expect(res).toEqual(mockSummary);
    });

    const req = httpTesting.expectOne((r) => r.url.endsWith(`/api/v1/comparisons/${comparisonId}`) || r.url.endsWith(`/api/comparisons/${comparisonId}`));
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);
  });

  it('should list comparisons', () => {
    const mockSummaries: ComparisonSummary[] = [
      {
        id: 'comp-1',
        status: 'COMPLETED',
        createdAt: '2026-08-28T00:00:00Z',
        ds1Type: 'FILE_UPLOAD',
        ds2Type: 'FILE_UPLOAD'
      }
    ];

    service.listComparisons().subscribe(res => {
      expect(res).toEqual(mockSummaries);
    });

    const req = httpTesting.expectOne((r) => r.url.endsWith('/api/v1/comparisons') || r.url.endsWith('/api/comparisons'));
    expect(req.request.method).toBe('GET');
    req.flush(mockSummaries);
  });

  it('should get headers', () => {
    const comparisonId = 'comp-123';
    const mockHeaders: DatasetColumns = { ds1: ['a', 'b'], ds2: ['a', 'b', 'c'] };

    service.getHeaders(comparisonId).subscribe(res => {
      expect(res).toEqual(mockHeaders);
    });

    const req = httpTesting.expectOne((r) => r.url.includes(`/api/v1/comparisons/${comparisonId}/headers`) || r.url.includes(`/api/comparisons/${comparisonId}/headers`));
    expect(req.request.method).toBe('GET');
    req.flush(mockHeaders);
  });

  it('should get paginated mismatches', () => {
    const comparisonId = 'comp-123';
    const mockPaged: PagedResult<any> = {
      content: [],
      page: 0,
      size: 50,
      totalElements: 0,
      totalPages: 0,
      last: true
    };

    service.getMismatches(comparisonId, 0, 50, 'DS1').subscribe(res => {
      expect(res).toEqual(mockPaged);
    });

    const req = httpTesting.expectOne((r) => r.url.includes(`/api/v1/comparisons/${comparisonId}/mismatches`) || r.url.includes(`/api/comparisons/${comparisonId}/mismatches`));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('50');
    expect(req.request.params.get('direction')).toBe('DS1');
    req.flush(mockPaged);
  });

  it('should get paginated missing', () => {
    const comparisonId = 'comp-123';
    const mockPaged: PagedResult<any> = {
      content: [],
      page: 0,
      size: 50,
      totalElements: 0,
      totalPages: 0,
      last: true
    };

    service.getMissing(comparisonId, 0, 50, 'DS1').subscribe(res => {
      expect(res).toEqual(mockPaged);
    });

    const req = httpTesting.expectOne((r) => r.url.includes(`/api/v1/comparisons/${comparisonId}/missing`) || r.url.includes(`/api/comparisons/${comparisonId}/missing`));
    expect(req.request.method).toBe('GET');
    req.flush(mockPaged);
  });

  it('should delete comparison', () => {
    const comparisonId = 'comp-123';

    service.deleteComparison(comparisonId).subscribe();

    const req = httpTesting.expectOne((r) => r.url.endsWith(`/api/v1/comparisons/${comparisonId}`) || r.url.endsWith(`/api/comparisons/${comparisonId}`));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should return report download url', () => {
    const comparisonId = 'comp-123';
    expect(service.getReportUrl(comparisonId)).toContain(`/api/v1/comparisons/${comparisonId}/report`);
  });
});
