import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { By } from '@angular/platform-browser';

import { HistoryComponent } from './history.component';
import { ComparisonService } from '../../services/comparison.service';
import { ComparisonSummary } from '../../models/comparison.model';

describe('HistoryComponent', () => {
  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;
  let comparisonServiceMock: any;
  let snackBarMock: any;
  let router: Router;

  const mockComparisons: ComparisonSummary[] = [
    {
      id: 'abc12345-6789-0000-0000-000000000001',
      status: 'COMPLETED',
      createdAt: '2026-08-28T12:00:00Z',
      completedAt: '2026-08-28T12:01:00Z',
      ds1Type: 'FILE_UPLOAD',
      ds1FileName: 'dataset1.csv',
      ds2Type: 'FILE_UPLOAD',
      ds2FileName: 'dataset2.csv',
      ds1RecordCount: 1500,
      ds2RecordCount: 1520,
      ds1FullyMatching: 1450,
      ds2FullyMatching: 1450,
      ds1NotMatching: 50,
      ds2NotMatching: 70
    },
    {
      id: 'def67890-1234-0000-0000-000000000002',
      status: 'COMPARING',
      createdAt: '2026-08-28T13:30:00Z',
      ds1Type: 'SQL_QUERY',
      ds2Type: 'SQL_QUERY',
      ds1RecordCount: 500,
      ds2RecordCount: 500
    },
    {
      id: 'ghi11111-2222-0000-0000-000000000003',
      status: 'FAILED',
      createdAt: '2026-08-28T14:00:00Z',
      ds1Type: 'FILE_UPLOAD',
      ds1FileName: 'orders.xlsx',
      ds2Type: 'SQL_QUERY',
      errorMessage: 'Database connection timed out'
    },
    {
      id: 'jkl33333-4444-0000-0000-000000000004',
      status: 'PENDING',
      createdAt: '2026-08-28T15:00:00Z',
      ds1Type: 'FILE_UPLOAD',
      ds1FileName: 'source.csv',
      ds2Type: 'FILE_UPLOAD',
      ds2FileName: 'target.csv'
    }
  ];

  beforeEach(async () => {
    comparisonServiceMock = {
      listComparisons: vi.fn().mockReturnValue(of(mockComparisons)),
      deleteComparison: vi.fn().mockReturnValue(of(undefined))
    };

    snackBarMock = {
      open: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [HistoryComponent],
      providers: [
        provideRouter([]),
        provideAnimationsAsync(),
        { provide: ComparisonService, useValue: comparisonServiceMock },
        { provide: MatSnackBar, useValue: snackBarMock }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));

    fixture = TestBed.createComponent(HistoryComponent);
    component = fixture.componentInstance;
  });

  it('should create and fetch comparisons on init', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
    expect(comparisonServiceMock.listComparisons).toHaveBeenCalledTimes(1);
    expect(component.comparisons().length).toBe(4);
    expect(component.isLoading()).toBe(false);
  });

  it('should render table with expected columns and rows', () => {
    fixture.detectChanges();
    const table = fixture.debugElement.query(By.css('table[mat-table]'));
    expect(table).toBeTruthy();

    const headerCells = fixture.debugElement.queryAll(By.css('th.mat-mdc-header-cell'));
    const headerTexts = headerCells.map((cell) => cell.nativeElement.textContent.trim());

    expect(headerTexts).toContain('ID');
    expect(headerTexts).toContain('Created');
    expect(headerTexts).toContain('Status');
    expect(headerTexts).toContain('DS1 Source');
    expect(headerTexts).toContain('DS2 Source');
    expect(headerTexts).toContain('Records');
    expect(headerTexts).toContain('Actions');

    const rows = fixture.debugElement.queryAll(By.css('tr.mat-mdc-row'));
    expect(rows.length).toBe(4);
  });

  it('should render short UUID and link to /results/:id', () => {
    fixture.detectChanges();
    const firstRowIdLink = fixture.debugElement.query(By.css('tr.mat-mdc-row a.id-link'));
    expect(firstRowIdLink).toBeTruthy();
    expect(firstRowIdLink.nativeElement.textContent.trim()).toBe('#jkl33333');
    expect(firstRowIdLink.attributes['href']).toContain('/results/jkl33333-4444-0000-0000-000000000004');
  });

  it('should render status chips with appropriate styling classes', () => {
    fixture.detectChanges();
    const chips = fixture.debugElement.queryAll(By.css('mat-chip'));
    expect(chips.length).toBe(4);

    const statuses = chips.map((c) => c.nativeElement.textContent.trim());
    expect(statuses).toContain('COMPLETED');
    expect(statuses).toContain('COMPARING');
    expect(statuses).toContain('FAILED');
    expect(statuses).toContain('PENDING');

    const completedChip = chips.find((c) => c.nativeElement.textContent.trim() === 'COMPLETED');
    expect(completedChip?.nativeElement.classList).toContain('status-completed');

    const comparingChip = chips.find((c) => c.nativeElement.textContent.trim() === 'COMPARING');
    expect(comparingChip?.nativeElement.classList).toContain('status-comparing');

    const failedChip = chips.find((c) => c.nativeElement.textContent.trim() === 'FAILED');
    expect(failedChip?.nativeElement.classList).toContain('status-failed');

    const pendingChip = chips.find((c) => c.nativeElement.textContent.trim() === 'PENDING');
    expect(pendingChip?.nativeElement.classList).toContain('status-pending');
  });

  it('should display correct DS1 and DS2 source labels', () => {
    fixture.detectChanges();
    const rows = fixture.debugElement.queryAll(By.css('tr.mat-mdc-row'));

    // Verify presence of SQL and file sources across rows
    const textContents = rows.map((r) => r.nativeElement.textContent);
    expect(textContents.some((t) => t.includes('dataset1.csv'))).toBe(true);
    expect(textContents.some((t) => t.includes('SQL Query'))).toBe(true);
    expect(textContents.some((t) => t.includes('orders.xlsx'))).toBe(true);
  });

  it('should format record counts properly', () => {
    fixture.detectChanges();
    const rows = fixture.debugElement.queryAll(By.css('tr.mat-mdc-row'));
    const textContents = rows.map((r) => r.nativeElement.textContent);

    expect(textContents.some((t) => t.includes('1,500 / 1,520'))).toBe(true);
    expect(textContents.some((t) => t.includes('- / -'))).toBe(true);
  });

  it('should navigate to results when View Results action button is clicked', () => {
    fixture.detectChanges();
    const viewButtons = fixture.debugElement.queryAll(By.css('button[data-testid="btn-view-results"]'));
    expect(viewButtons.length).toBe(4);

    viewButtons[0].nativeElement.click();
    expect(router.navigate).toHaveBeenCalledWith(['/results', component.comparisons()[0].id]);
  });

  it('should call delete API and remove row when Delete action button is clicked', () => {
    fixture.detectChanges();
    const targetId = component.comparisons()[0].id;
    const deleteButtons = fixture.debugElement.queryAll(By.css('button[data-testid="btn-delete-comparison"]'));
    expect(deleteButtons.length).toBe(4);

    deleteButtons[0].nativeElement.click();
    expect(comparisonServiceMock.deleteComparison).toHaveBeenCalledWith(targetId);

    fixture.detectChanges();
    expect(component.comparisons().length).toBe(3);
    expect(component.comparisons().find((c) => c.id === targetId)).toBeUndefined();
    expect(snackBarMock.open).toHaveBeenCalledWith(
      expect.stringMatching(/deleted/i),
      'Dismiss',
      expect.any(Object)
    );
  });

  it('should show empty state message and New Comparison button when no comparisons exist', () => {
    comparisonServiceMock.listComparisons.mockReturnValue(of([]));
    component.loadComparisons();
    fixture.detectChanges();

    const emptyState = fixture.debugElement.query(By.css('[data-testid="empty-history-state"]'));
    expect(emptyState).toBeTruthy();
    expect(emptyState.nativeElement.textContent).toContain('No comparisons found');

    const newComparisonBtn = fixture.debugElement.query(By.css('[data-testid="btn-start-first-comparison"]'));
    expect(newComparisonBtn).toBeTruthy();
    expect(newComparisonBtn.attributes['href']).toContain('/compare');
  });

  it('should render header New Comparison button navigating to /compare', () => {
    fixture.detectChanges();
    const headerNewBtn = fixture.debugElement.query(By.css('[data-testid="btn-new-comparison"]'));
    expect(headerNewBtn).toBeTruthy();
    expect(headerNewBtn.attributes['href']).toContain('/compare');
  });

  it('should handle API error gracefully', () => {
    comparisonServiceMock.listComparisons.mockReturnValue(throwError(() => new Error('Server error')));
    component.loadComparisons();
    fixture.detectChanges();

    expect(component.errorMessage()).toContain('Server error');
    const errorBanner = fixture.debugElement.query(By.css('.error-banner'));
    expect(errorBanner).toBeTruthy();
  });

  it('should auto-refresh comparisons on interval', () => {
    vi.useFakeTimers();
    fixture.detectChanges();
    expect(comparisonServiceMock.listComparisons).toHaveBeenCalledTimes(1);

    vi.advanceTimersByTime(30000);
    expect(comparisonServiceMock.listComparisons).toHaveBeenCalledTimes(2);

    vi.advanceTimersByTime(30000);
    expect(comparisonServiceMock.listComparisons).toHaveBeenCalledTimes(3);

    component.ngOnDestroy();
    vi.advanceTimersByTime(30000);
    expect(comparisonServiceMock.listComparisons).toHaveBeenCalledTimes(3);

    vi.useRealTimers();
  });
});
