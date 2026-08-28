import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResultsComponent } from './results.component';
import { ComparisonService } from '../../services/comparison.service';
import { ProgressService } from '../../services/progress.service';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { BehaviorSubject, of, Subject, throwError } from 'rxjs';
import { ComparisonSummary, ProgressUpdate } from '../../models/comparison.model';

describe('ResultsComponent', () => {
  let component: ResultsComponent;
  let fixture: ComponentFixture<ResultsComponent>;
  let comparisonServiceMock: any;
  let progressServiceMock: any;
  let progressSubject: Subject<ProgressUpdate>;
  let router: Router;

  const mockCompletedSummary: ComparisonSummary = {
    id: 'test-comp-123',
    status: 'COMPLETED',
    createdAt: '2026-08-28T00:00:00Z',
    completedAt: '2026-08-28T00:02:00Z',
    ds1FileName: 'sales_2025.csv',
    ds2FileName: 'sales_2026.csv',
    ds1RecordCount: 5000,
    ds2RecordCount: 5200,
    ds1FullyMatching: 4800,
    ds2FullyMatching: 4800,
    ds1NotMatching: 120,
    ds2NotMatching: 280,
    ds1MissingInDs2: 80,
    ds2MissingInDs1: 120
  };

  beforeEach(async () => {
    // Canvas 2d context stub for JSDOM
    if (!HTMLCanvasElement.prototype.getContext) {
      HTMLCanvasElement.prototype.getContext = (() => ({
        fillRect: () => {},
        clearRect: () => {},
        getImageData: () => ({ data: [] }),
        putImageData: () => {},
        createImageData: () => [],
        setTransform: () => {},
        drawImage: () => {},
        save: () => {},
        fillText: () => {},
        restore: () => {},
        beginPath: () => {},
        moveTo: () => {},
        lineTo: () => {},
        closePath: () => {},
        stroke: () => {},
        translate: () => {},
        scale: () => {},
        rotate: () => {},
        arc: () => {},
        fill: () => {},
        measureText: () => ({ width: 0 }),
        transform: () => {},
        rect: () => {},
        clip: () => {}
      })) as any;
    }

    progressSubject = new Subject<ProgressUpdate>();

    comparisonServiceMock = {
      getComparison: vi.fn().mockReturnValue(of(mockCompletedSummary)),
      getReportUrl: vi.fn().mockReturnValue('/api/v1/comparisons/test-comp-123/report')
    };

    progressServiceMock = {
      subscribe: vi.fn().mockReturnValue(progressSubject.asObservable())
    };

    await TestBed.configureTestingModule({
      imports: [ResultsComponent],
      providers: [
        provideRouter([]),
        provideAnimationsAsync(),
        provideCharts(withDefaultRegisterables()),
        { provide: ComparisonService, useValue: comparisonServiceMock },
        { provide: ProgressService, useValue: progressServiceMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'comparisonId' ? 'test-comp-123' : null)
              }
            },
            paramMap: of(new Map([['comparisonId', 'test-comp-123']]))
          }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(ResultsComponent);
    component = fixture.componentInstance;
  });

  it('should create and load completed summary data on init', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(comparisonServiceMock.getComparison).toHaveBeenCalledWith('test-comp-123');
    expect(progressServiceMock.subscribe).toHaveBeenCalledWith('test-comp-123');
    expect(component.summary()).toEqual(mockCompletedSummary);
    expect(component.isLoading()).toBe(false);

    // Summary cards and chart are rendered
    const summaryCards = fixture.nativeElement.querySelector('app-summary-cards');
    const summaryChart = fixture.nativeElement.querySelector('app-summary-chart');
    expect(summaryCards).toBeTruthy();
    expect(summaryChart).toBeTruthy();
  });

  it('should display progress bar with stage and percentage during active progress stream', () => {
    const inProgressSummary: ComparisonSummary = {
      id: 'test-comp-123',
      status: 'COMPARING',
      createdAt: '2026-08-28T00:00:00Z'
    };
    comparisonServiceMock.getComparison.mockReturnValue(of(inProgressSummary));

    fixture.detectChanges();

    expect(component.isLoading()).toBe(true);

    // Emit progress event
    progressSubject.next({ stage: 'Comparing records', percent: 65 });
    fixture.detectChanges();

    expect(component.currentStage()).toBe('Comparing records');
    expect(component.progressPercent()).toBe(65);

    const progressBar = fixture.nativeElement.querySelector('mat-progress-bar');
    const stageLabel = fixture.nativeElement.querySelector('.progress-stage');
    expect(progressBar).toBeTruthy();
    expect(stageLabel.textContent).toContain('Comparing records');
    expect(stageLabel.textContent).toContain('65%');
  });

  it('should transition from in-progress to completed when SSE emits COMPLETED', () => {
    const inProgressSummary: ComparisonSummary = {
      id: 'test-comp-123',
      status: 'CONVERTING',
      createdAt: '2026-08-28T00:00:00Z'
    };
    comparisonServiceMock.getComparison.mockReturnValue(of(inProgressSummary));

    fixture.detectChanges();
    expect(component.isLoading()).toBe(true);

    // Prepare getComparison for when COMPLETED arrives
    comparisonServiceMock.getComparison.mockReturnValue(of(mockCompletedSummary));

    // Emit COMPLETED
    progressSubject.next({ stage: 'COMPLETED', percent: 100 });
    progressSubject.complete();
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.summary()).toEqual(mockCompletedSummary);

    const summaryCards = fixture.nativeElement.querySelector('app-summary-cards');
    expect(summaryCards).toBeTruthy();
  });

  it('should display error banner when comparison fails or SSE emits error', () => {
    const inProgressSummary: ComparisonSummary = {
      id: 'test-comp-123',
      status: 'COMPARING',
      createdAt: '2026-08-28T00:00:00Z'
    };
    comparisonServiceMock.getComparison.mockReturnValue(of(inProgressSummary));

    fixture.detectChanges();

    progressSubject.error(new Error('Comparison processing error occurred'));
    fixture.detectChanges();

    expect(component.errorMessage()).toContain('Comparison processing error occurred');
    expect(component.isLoading()).toBe(false);

    const errorBanner = fixture.nativeElement.querySelector('.error-banner');
    expect(errorBanner).toBeTruthy();
    expect(errorBanner.textContent).toContain('Comparison processing error occurred');
  });

  it('should display error when SSE emits FAILED stage with message', () => {
    const inProgressSummary: ComparisonSummary = {
      id: 'test-comp-123',
      status: 'COMPARING',
      createdAt: '2026-08-28T00:00:00Z'
    };
    comparisonServiceMock.getComparison.mockReturnValue(of(inProgressSummary));

    fixture.detectChanges();

    progressSubject.next({ stage: 'FAILED', percent: 100, message: 'DuckDB comparison failed on invalid column' });
    fixture.detectChanges();

    expect(component.errorMessage()).toContain('DuckDB comparison failed on invalid column');
    expect(component.isLoading()).toBe(false);

    const errorBanner = fixture.nativeElement.querySelector('.error-banner');
    expect(errorBanner).toBeTruthy();
    expect(errorBanner.textContent).toContain('DuckDB comparison failed on invalid column');
  });

  it('should render toolbar with Back to Compare link and Download Report button', () => {
    fixture.detectChanges();

    const backBtn = fixture.nativeElement.querySelector('[data-testid="btn-back-to-compare"]');
    const downloadBtn = fixture.nativeElement.querySelector('[data-testid="btn-download-report"]');

    expect(backBtn).toBeTruthy();
    expect(downloadBtn).toBeTruthy();
  });

  it('should trigger report download url on Download Report button click', () => {
    fixture.detectChanges();

    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null);
    const downloadBtn = fixture.nativeElement.querySelector('[data-testid="btn-download-report"]') as HTMLButtonElement;
    downloadBtn.click();

    expect(comparisonServiceMock.getReportUrl).toHaveBeenCalledWith('test-comp-123');
    expect(openSpy).toHaveBeenCalledWith('/api/v1/comparisons/test-comp-123/report', '_blank');
  });
});
