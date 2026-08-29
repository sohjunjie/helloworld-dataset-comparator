import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompareComponent } from './compare.component';
import { ComparisonService } from '../../services/comparison.service';
import { ProgressService } from '../../services/progress.service';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { from, of, throwError } from 'rxjs';
import { UploadResponse, ComparisonSummary, ProgressUpdate } from '../../models/comparison.model';

describe('CompareComponent', () => {
  let component: CompareComponent;
  let fixture: ComponentFixture<CompareComponent>;
  let comparisonServiceMock: any;
  let progressServiceMock: any;
  let router: Router;

  const mockUploadResponse: UploadResponse = {
    comparisonId: 'comp-12345',
    columns: {
      ds1: ['id', 'name', 'salary'],
      ds2: ['id', 'name', 'salary', 'department']
    }
  };

  const mockExecuteResponse: ComparisonSummary = {
    id: 'comp-12345',
    status: 'COMPLETED',
    createdAt: '2026-08-28T00:00:00Z',
    ds1Type: 'FILE_UPLOAD',
    ds2Type: 'FILE_UPLOAD'
  };

  beforeEach(async () => {
    comparisonServiceMock = {
      upload: vi.fn().mockReturnValue(of(mockUploadResponse)),
      execute: vi.fn().mockReturnValue(of(mockExecuteResponse))
    };

    const progressUpdates: ProgressUpdate[] = [
      { stage: 'CONVERTING', percent: 20 },
      { stage: 'COMPARING', percent: 80 },
      { stage: 'COMPLETED', percent: 100 }
    ];

    progressServiceMock = {
      subscribe: vi.fn().mockReturnValue(from(progressUpdates))
    };

    await TestBed.configureTestingModule({
      imports: [CompareComponent],
      providers: [
        provideRouter([]),
        provideAnimationsAsync(),
        { provide: ComparisonService, useValue: comparisonServiceMock },
        { provide: ProgressService, useValue: progressServiceMock }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(CompareComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and display side-by-side dataset input panels', () => {
    expect(component).toBeTruthy();
    const datasetInputs = fixture.nativeElement.querySelectorAll('app-dataset-input');
    expect(datasetInputs.length).toBe(2);
  });

  it('should have upload button disabled until both datasets are valid', () => {
    const uploadBtn = fixture.nativeElement.querySelector('.upload-btn') as HTMLButtonElement;
    expect(uploadBtn.disabled).toBe(true);

    const file1 = new File(['1,Alice'], 'ds1.csv', { type: 'text/csv' });
    component.onDs1FileChanged(file1);
    component.ds1InputComponent?.onFileSelected(file1);
    fixture.detectChanges();
    expect(uploadBtn.disabled).toBe(true);

    const file2 = new File(['1,Bob'], 'ds2.csv', { type: 'text/csv' });
    component.onDs2FileChanged(file2);
    component.ds2InputComponent?.onFileSelected(file2);
    fixture.detectChanges();
    expect(uploadBtn.disabled).toBe(false);
  });

  it('should upload files and transition to step 2 with auto-detected columns', async () => {
    const file1 = new File(['id,name,salary'], 'ds1.csv', { type: 'text/csv' });
    const file2 = new File(['id,name,salary,department'], 'ds2.csv', { type: 'text/csv' });
    component.ds1InputComponent?.onFileSelected(file1);
    component.ds2InputComponent?.onFileSelected(file2);
    fixture.detectChanges();

    const uploadBtn = fixture.nativeElement.querySelector('.upload-btn') as HTMLButtonElement;
    uploadBtn.click();
    fixture.detectChanges();

    expect(comparisonServiceMock.upload).toHaveBeenCalledWith({
      ds1File: file1,
      ds2File: file2,
      ds1Delimiter: 'AUTO',
      ds2Delimiter: 'AUTO',
      ds1Sql: undefined,
      ds1Connection: undefined,
      ds2Sql: undefined,
      ds2Connection: undefined
    });
    expect(component.currentStep()).toBe(2);
    expect(component.comparisonId()).toBe('comp-12345');
    expect(component.availableColumns()).toEqual(['id', 'name', 'salary', 'department']);

    // Column selector and tolerance sections are now rendered
    const colSelector = fixture.nativeElement.querySelector('app-column-selector');
    const tolConfig = fixture.nativeElement.querySelector('app-tolerance-config');
    expect(colSelector).toBeTruthy();
    expect(tolConfig).toBeTruthy();
  });

  it('should toggle to SQL mode, fill in editor and connection fields, upload datasets via SQL config, and populate column selector', async () => {
    const ds1 = component.ds1InputComponent!;
    const ds2 = component.ds2InputComponent!;

    // Toggle both datasets to SQL_QUERY mode
    ds1.setSourceType('SQL_QUERY');
    ds2.setSourceType('SQL_QUERY');
    fixture.detectChanges();

    expect(ds1.sourceType()).toBe('SQL_QUERY');
    expect(ds2.sourceType()).toBe('SQL_QUERY');

    // Fill in SQL queries and connection fields
    ds1.onSqlQueryChanged('SELECT id, name, salary FROM staff');
    ds1.host.set('pg-host-1');
    ds1.port.set(5432);
    ds1.database.set('inventory_db');
    ds1.username.set('user1');
    ds1.password.set('pass1');

    ds2.onSqlQueryChanged('SELECT id, name, salary, department FROM employees');
    ds2.host.set('pg-host-2');
    ds2.port.set(5432);
    ds2.database.set('warehouse_db');
    ds2.username.set('user2');
    ds2.password.set('pass2');

    fixture.detectChanges();

    expect(component.canUpload()).toBe(true);

    const uploadBtn = fixture.nativeElement.querySelector('.upload-btn') as HTMLButtonElement;
    expect(uploadBtn.disabled).toBe(false);
    uploadBtn.click();
    fixture.detectChanges();

    expect(comparisonServiceMock.upload).toHaveBeenCalledWith({
      ds1File: null,
      ds2File: null,
      ds1Delimiter: undefined,
      ds2Delimiter: undefined,
      ds1Sql: 'SELECT id, name, salary FROM staff',
      ds1Connection: {
        host: 'pg-host-1',
        port: 5432,
        database: 'inventory_db',
        username: 'user1',
        password: 'pass1'
      },
      ds2Sql: 'SELECT id, name, salary, department FROM employees',
      ds2Connection: {
        host: 'pg-host-2',
        port: 5432,
        database: 'warehouse_db',
        username: 'user2',
        password: 'pass2'
      }
    });

    expect(component.currentStep()).toBe(2);
    expect(component.comparisonId()).toBe('comp-12345');
    expect(component.availableColumns()).toEqual(['id', 'name', 'salary', 'department']);

    const colSelector = fixture.nativeElement.querySelector('app-column-selector');
    expect(colSelector).toBeTruthy();
  });

  it('should support mixed mode where DS1 is file upload and DS2 is SQL query', async () => {
    const ds1 = component.ds1InputComponent!;
    const ds2 = component.ds2InputComponent!;

    // DS1 from File
    const file1 = new File(['id,name\n1,Alice'], 'ds1.csv', { type: 'text/csv' });
    ds1.onFileSelected(file1);

    // DS2 from SQL
    ds2.setSourceType('SQL_QUERY');
    ds2.onSqlQueryChanged('SELECT id, name FROM remote_db');
    ds2.host.set('pg-host');
    ds2.port.set(5432);
    ds2.database.set('remote_db');
    ds2.username.set('user');
    ds2.password.set('pass');

    fixture.detectChanges();
    expect(component.canUpload()).toBe(true);

    component.uploadFiles();
    fixture.detectChanges();

    expect(comparisonServiceMock.upload).toHaveBeenCalledWith({
      ds1File: file1,
      ds2File: null,
      ds1Delimiter: 'AUTO',
      ds2Delimiter: undefined,
      ds1Sql: undefined,
      ds1Connection: undefined,
      ds2Sql: 'SELECT id, name FROM remote_db',
      ds2Connection: {
        host: 'pg-host',
        port: 5432,
        database: 'remote_db',
        username: 'user',
        password: 'pass'
      }
    });
    expect(component.currentStep()).toBe(2);
  });

  it('should require at least one key column before enabling compare button', () => {
    component.currentStep.set(2);
    component.comparisonId.set('comp-12345');
    component.availableColumns.set(['id', 'name']);
    fixture.detectChanges();

    const compareBtn = fixture.nativeElement.querySelector('.compare-btn') as HTMLButtonElement;
    expect(compareBtn.disabled).toBe(true);

    component.onKeyColumnsChanged(['id']);
    fixture.detectChanges();
    expect(compareBtn.disabled).toBe(false);
  });

  it('should disable compare button if a tolerance row has invalid percentage', () => {
    component.currentStep.set(2);
    component.comparisonId.set('comp-12345');
    component.availableColumns.set(['id', 'salary']);
    component.onKeyColumnsChanged(['id']);
    fixture.detectChanges();

    const compareBtn = fixture.nativeElement.querySelector('.compare-btn') as HTMLButtonElement;
    expect(compareBtn.disabled).toBe(false);

    // Add invalid tolerance
    const tolComp = component.toleranceConfigComponent;
    if (tolComp) {
      tolComp.addTolerance();
      tolComp.updateColumn(0, 'salary');
      tolComp.updatePercentage(0, 150); // > 100 invalid
      fixture.detectChanges();

      expect(tolComp.isValid()).toBe(false);
      expect(component.canCompare()).toBe(false);
      expect(compareBtn.disabled).toBe(true);
    }
  });

  it('should disable upload button if custom delimiter is empty or multi-character', () => {
    const file1 = new File(['1,Alice'], 'ds1.csv', { type: 'text/csv' });
    const file2 = new File(['1,Bob'], 'ds2.csv', { type: 'text/csv' });
    component.ds1InputComponent?.onFileSelected(file1);
    component.ds2InputComponent?.onFileSelected(file2);
    fixture.detectChanges();

    const uploadBtn = fixture.nativeElement.querySelector('.upload-btn') as HTMLButtonElement;
    expect(uploadBtn.disabled).toBe(false);

    // Set custom delimiter to empty string
    component.ds1InputComponent!.delimiterType.set('CUSTOM');
    component.ds1InputComponent!.customDelimiter.set('');
    fixture.detectChanges();

    expect(component.ds1InputComponent!.isValid()).toBe(false);
    expect(component.canUpload()).toBe(false);
    expect(uploadBtn.disabled).toBe(true);
  });


  it('should execute comparison, track SSE progress, and navigate to results page on completion', async () => {
    component.currentStep.set(2);
    component.comparisonId.set('comp-12345');
    component.availableColumns.set(['id', 'name']);
    component.onKeyColumnsChanged(['id']);
    component.onTolerancesChanged([{ columnName: 'salary', percentage: 2.0 }]);
    component.caseSensitive.set(true);
    fixture.detectChanges();

    const compareBtn = fixture.nativeElement.querySelector('.compare-btn') as HTMLButtonElement;
    compareBtn.click();
    fixture.detectChanges();

    expect(comparisonServiceMock.execute).toHaveBeenCalledWith('comp-12345', {
      keyColumns: ['id'],
      tolerances: [{ columnName: 'salary', percentage: 2.0 }],
      caseSensitive: true
    });
    expect(progressServiceMock.subscribe).toHaveBeenCalledWith('comp-12345');
    expect(router.navigate).toHaveBeenCalledWith(['/results', 'comp-12345']);
  });

  it('should display error message if upload fails', () => {
    comparisonServiceMock.upload.mockReturnValue(throwError(() => new Error('Upload network error')));
    const file1 = new File(['1'], 'ds1.csv', { type: 'text/csv' });
    const file2 = new File(['2'], 'ds2.csv', { type: 'text/csv' });
    component.ds1InputComponent?.onFileSelected(file1);
    component.ds2InputComponent?.onFileSelected(file2);
    fixture.detectChanges();

    component.uploadFiles();
    fixture.detectChanges();

    expect(component.errorMessage()).toContain('Upload network error');
    expect(component.currentStep()).toBe(1);
  });

  it('should handle comparison failure from execute call or SSE update', () => {
    component.currentStep.set(2);
    component.comparisonId.set('comp-12345');
    component.availableColumns.set(['id']);
    component.selectedKeyColumns.set(['id']);
    comparisonServiceMock.execute.mockReturnValue(throwError(() => ({ error: { message: 'DuckDB out of memory' } })));
    fixture.detectChanges();

    component.startComparison();
    fixture.detectChanges();

    expect(component.isComparing()).toBe(false);
    expect(component.errorMessage()).toContain('DuckDB out of memory');
  });

  it('should handle FAILED stage from SSE stream and display error', () => {
    component.currentStep.set(2);
    component.comparisonId.set('comp-12345');
    component.availableColumns.set(['id']);
    component.selectedKeyColumns.set(['id']);
    comparisonServiceMock.execute.mockReturnValue(of({ id: 'comp-12345', status: 'COMPARING' }));
    progressServiceMock.subscribe.mockReturnValue(of({ stage: 'FAILED', percent: 100, message: 'Column mismatch failure' }));
    fixture.detectChanges();

    component.startComparison();
    fixture.detectChanges();

    expect(component.isComparing()).toBe(false);
    expect(component.errorMessage()).toContain('Column mismatch failure');
  });

  it('should reset form on Start Over', () => {
    component.currentStep.set(2);
    component.comparisonId.set('comp-12345');
    component.availableColumns.set(['id']);
    component.selectedKeyColumns.set(['id']);
    fixture.detectChanges();

    component.resetForm();
    fixture.detectChanges();

    expect(component.currentStep()).toBe(1);
    expect(component.comparisonId()).toBeNull();
    expect(component.availableColumns()).toEqual([]);
    expect(component.selectedKeyColumns()).toEqual([]);
  });
});
