import '../../../../test-setup';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';

import { DetailTableComponent } from './detail-table.component';
import { ComparisonService } from '../../../services/comparison.service';
import { MismatchDetail, MissingDetail, PagedResult } from '../../../models/comparison.model';

describe('DetailTableComponent', () => {
  let component: DetailTableComponent;
  let fixture: ComponentFixture<DetailTableComponent>;
  let comparisonServiceMock: any;

  const mockMismatchRows: MismatchDetail[] = [
    {
      rowNumberDs1: 1,
      rowNumberDs2: 1,
      keyValues: { id: 101 },
      dataDs1: { id: 101, customer: 'Alice', amount: 100.5, status: 'PENDING' },
      dataDs2: { id: 101, customer: 'Alice', amount: 105.0, status: 'COMPLETED' },
      differingColumns: ['amount', 'status']
    },
    {
      rowNumberDs1: 2,
      rowNumberDs2: 2,
      keyValues: { id: 102 },
      dataDs1: { id: 102, customer: 'Bob', amount: 250.0, status: 'ACTIVE' },
      dataDs2: { id: 102, customer: 'Bob', amount: 250.0, status: 'ACTIVE' },
      differingColumns: []
    }
  ];

  const mockMismatchPagedResult: PagedResult<MismatchDetail> = {
    content: mockMismatchRows,
    page: 0,
    size: 50,
    totalElements: 2,
    totalPages: 1,
    last: true
  };

  const mockMissingRowsDs2: MissingDetail[] = [
    {
      rowNumber: 1,
      keyValues: { id: 201 },
      data: { id: 201, customer: 'Charlie', amount: 300.0, status: 'NEW' },
      missingFrom: 'DS2',
      direction: 'DS1'
    }
  ];

  const mockMissingPagedResultDs2: PagedResult<MissingDetail> = {
    content: mockMissingRowsDs2,
    page: 0,
    size: 50,
    totalElements: 1,
    totalPages: 1,
    last: true
  };

  const mockMissingRowsDs1: MissingDetail[] = [
    {
      rowNumber: 1,
      keyValues: { id: 301 },
      data: { id: 301, customer: 'Diana', amount: 450.0, region: 'EMEA' },
      missingFrom: 'DS1',
      direction: 'DS2'
    }
  ];

  const mockMissingPagedResultDs1: PagedResult<MissingDetail> = {
    content: mockMissingRowsDs1,
    page: 0,
    size: 50,
    totalElements: 1,
    totalPages: 1,
    last: true
  };

  beforeEach(async () => {
    comparisonServiceMock = {
      getHeaders: vi.fn().mockReturnValue(of({ ds1: ['id', 'customer', 'amount', 'status'], ds2: ['id', 'customer', 'amount', 'status'] })),
      getMismatches: vi.fn().mockReturnValue(of(mockMismatchPagedResult)),
      getMissing: vi.fn().mockImplementation((id: string, page: number, size: number, direction: string) => {
        if (direction === 'ds2') {
          return of(mockMissingPagedResultDs1);
        }
        return of(mockMissingPagedResultDs2);
      })
    };

    await TestBed.configureTestingModule({
      imports: [DetailTableComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: ComparisonService, useValue: comparisonServiceMock }
      ]
    }).compileComponents();
  });

  function setupComponent(comparisonId = 'test-comp-123', resultType: 'mismatches' | 'missing_from_ds2' | 'missing_from_ds1' = 'mismatches') {
    fixture = TestBed.createComponent(DetailTableComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('comparisonId', comparisonId);
    fixture.componentRef.setInput('resultType', resultType);
    fixture.detectChanges();
  }

  it('should create and load mismatches data by default', () => {
    setupComponent('test-comp-123', 'mismatches');

    expect(component).toBeTruthy();
    expect(comparisonServiceMock.getMismatches).toHaveBeenCalledWith('test-comp-123', 0, 50);
    expect(component.mismatchRows().length).toBe(2);
    expect(component.totalElements()).toBe(2);
    expect(component.isLoading()).toBe(false);

    // Verify side-by-side table is rendered
    const table = fixture.nativeElement.querySelector('[data-testid="mismatches-table"]');
    expect(table).toBeTruthy();

    // Verify dynamic column headers for DS1 and DS2
    expect(component.ds1Columns()).toContain('id');
    expect(component.ds1Columns()).toContain('customer');
    expect(component.ds1Columns()).toContain('amount');
    expect(component.ds1Columns()).toContain('status');

    expect(component.ds2Columns()).toContain('id');
    expect(component.ds2Columns()).toContain('customer');
    expect(component.ds2Columns()).toContain('amount');
    expect(component.ds2Columns()).toContain('status');
  });

  it('should highlight differing cells with cell-mismatched CSS class', () => {
    setupComponent('test-comp-123', 'mismatches');

    // First row has differing amount and status
    const ds1AmountCell = fixture.nativeElement.querySelector('[data-testid="ds1-cell-amount"]');
    const ds2AmountCell = fixture.nativeElement.querySelector('[data-testid="ds2-cell-amount"]');
    const ds1CustomerCell = fixture.nativeElement.querySelector('[data-testid="ds1-cell-customer"]');

    expect(ds1AmountCell.classList).toContain('cell-mismatched');
    expect(ds2AmountCell.classList).toContain('cell-mismatched');
    expect(ds1CustomerCell.classList).not.toContain('cell-mismatched');
  });

  it('should load and render missing from DS2 records', () => {
    setupComponent('test-comp-123', 'missing_from_ds2');

    expect(comparisonServiceMock.getMissing).toHaveBeenCalledWith('test-comp-123', 0, 50, 'ds1');
    expect(component.missingRows().length).toBe(1);
    expect(component.missingColumns()).toContain('customer');
    expect(component.missingColumns()).toContain('status');

    const table = fixture.nativeElement.querySelector('[data-testid="missing-table"]');
    expect(table).toBeTruthy();

    const customerCell = fixture.nativeElement.querySelector('[data-testid="missing-cell-customer"]');
    expect(customerCell.textContent).toContain('Charlie');
  });

  it('should load and render missing from DS1 records', () => {
    setupComponent('test-comp-123', 'missing_from_ds1');

    expect(comparisonServiceMock.getMissing).toHaveBeenCalledWith('test-comp-123', 0, 50, 'ds2');
    expect(component.missingRows().length).toBe(1);
    expect(component.missingColumns()).toContain('region');

    const table = fixture.nativeElement.querySelector('[data-testid="missing-table"]');
    expect(table).toBeTruthy();

    const regionCell = fixture.nativeElement.querySelector('[data-testid="missing-cell-region"]');
    expect(regionCell.textContent).toContain('EMEA');
  });

  it('should trigger server-side pagination on page event', () => {
    setupComponent('test-comp-123', 'mismatches');

    const pageEvent: PageEvent = {
      pageIndex: 1,
      pageSize: 25,
      length: 100
    };

    component.onPageChange(pageEvent);
    fixture.detectChanges();

    expect(component.pageIndex()).toBe(1);
    expect(component.pageSize()).toBe(25);
    expect(comparisonServiceMock.getMismatches).toHaveBeenCalledWith('test-comp-123', 1, 25);
  });

  it('should display empty state when result set is empty', () => {
    const emptyPaged: PagedResult<MismatchDetail> = {
      content: [],
      page: 0,
      size: 50,
      totalElements: 0,
      totalPages: 0,
      last: true
    };
    comparisonServiceMock.getMismatches.mockReturnValue(of(emptyPaged));

    setupComponent('test-comp-123', 'mismatches');

    const emptyState = fixture.nativeElement.querySelector('[data-testid="empty-state"]');
    expect(emptyState).toBeTruthy();
    expect(emptyState.textContent).toContain('No Records Found');
  });

  it('should display error message and allow retry on API failure', () => {
    comparisonServiceMock.getMismatches.mockReturnValue(throwError(() => new Error('Server connection failed')));

    setupComponent('test-comp-123', 'mismatches');

    expect(component.isLoading()).toBe(false);
    expect(component.errorMessage()).toContain('Server connection failed');

    const errorBanner = fixture.nativeElement.querySelector('[data-testid="table-error"]');
    expect(errorBanner).toBeTruthy();
    expect(errorBanner.textContent).toContain('Server connection failed');

    // Retry
    comparisonServiceMock.getMismatches.mockReturnValue(of(mockMismatchPagedResult));
    const retryBtn = errorBanner.querySelector('button');
    retryBtn.click();
    fixture.detectChanges();

    expect(component.isLoading()).toBe(false);
    expect(component.errorMessage()).toBeNull();
    expect(component.mismatchRows().length).toBe(2);
  });
});
