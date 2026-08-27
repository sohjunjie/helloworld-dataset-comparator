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

  it('should have upload button disabled until both files are selected', () => {
    const uploadBtn = fixture.nativeElement.querySelector('.upload-btn') as HTMLButtonElement;
    expect(uploadBtn.disabled).toBe(true);

    const file1 = new File(['1,Alice'], 'ds1.csv', { type: 'text/csv' });
    component.onDs1FileChanged(file1);
    fixture.detectChanges();
    expect(uploadBtn.disabled).toBe(true);

    const file2 = new File(['1,Bob'], 'ds2.csv', { type: 'text/csv' });
    component.onDs2FileChanged(file2);
    fixture.detectChanges();
    expect(uploadBtn.disabled).toBe(false);
  });

  it('should upload files and transition to step 2 with auto-detected columns', async () => {
    const file1 = new File(['id,name,salary'], 'ds1.csv', { type: 'text/csv' });
    const file2 = new File(['id,name,salary,department'], 'ds2.csv', { type: 'text/csv' });
    component.onDs1FileChanged(file1);
    component.onDs2FileChanged(file2);
    fixture.detectChanges();

    const uploadBtn = fixture.nativeElement.querySelector('.upload-btn') as HTMLButtonElement;
    uploadBtn.click();
    fixture.detectChanges();

    expect(comparisonServiceMock.upload).toHaveBeenCalledWith(file1, file2, 'AUTO', 'AUTO');
    expect(component.currentStep()).toBe(2);
    expect(component.comparisonId()).toBe('comp-12345');
    expect(component.availableColumns()).toEqual(['id', 'name', 'salary', 'department']);

    // Column selector and tolerance sections are now rendered
    const colSelector = fixture.nativeElement.querySelector('app-column-selector');
    const tolConfig = fixture.nativeElement.querySelector('app-tolerance-config');
    expect(colSelector).toBeTruthy();
    expect(tolConfig).toBeTruthy();
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
    component.onDs1FileChanged(file1);
    component.onDs2FileChanged(file2);
    fixture.detectChanges();

    component.uploadFiles();
    fixture.detectChanges();

    expect(component.errorMessage()).toContain('Upload network error');
    expect(component.currentStep()).toBe(1);
  });
});
