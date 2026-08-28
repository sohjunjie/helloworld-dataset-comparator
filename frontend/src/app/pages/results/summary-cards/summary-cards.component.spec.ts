import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SummaryCardsComponent } from './summary-cards.component';
import { ComparisonSummary } from '../../../models/comparison.model';

describe('SummaryCardsComponent', () => {
  let component: SummaryCardsComponent;
  let fixture: ComponentFixture<SummaryCardsComponent>;

  const mockSummary: ComparisonSummary = {
    id: 'test-comp-1',
    status: 'COMPLETED',
    createdAt: '2026-08-28T00:00:00Z',
    completedAt: '2026-08-28T00:01:00Z',
    ds1Type: 'FILE_UPLOAD',
    ds1FileName: 'ds1.csv',
    ds2Type: 'FILE_UPLOAD',
    ds2FileName: 'ds2.csv',
    ds1RecordCount: 10000,
    ds2RecordCount: 10234,
    ds1FullyMatching: 9800,
    ds2FullyMatching: 9800,
    ds1NotMatching: 150,
    ds2NotMatching: 350,
    ds1MissingInDs2: 50,
    ds2MissingInDs1: 84
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SummaryCardsComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SummaryCardsComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('summary', mockSummary);
    fixture.detectChanges();
  });

  it('should create and render 6 mat-cards', () => {
    expect(component).toBeTruthy();
    const cards = fixture.nativeElement.querySelectorAll('mat-card');
    expect(cards.length).toBe(6);
  });

  it('should display DS1 and DS2 total record counts', () => {
    const ds1Card = fixture.nativeElement.querySelector('[data-testid="card-ds1-total"]');
    const ds2Card = fixture.nativeElement.querySelector('[data-testid="card-ds2-total"]');

    expect(ds1Card).toBeTruthy();
    expect(ds2Card).toBeTruthy();
    expect(ds1Card.textContent).toContain('10,000');
    expect(ds2Card.textContent).toContain('10,234');
  });

  it('should display fully matching counts', () => {
    const matchCard = fixture.nativeElement.querySelector('[data-testid="card-matching"]');
    expect(matchCard).toBeTruthy();
    expect(matchCard.textContent).toContain('9,800');
  });

  it('should display not matching counts for both directions', () => {
    const notMatchCard = fixture.nativeElement.querySelector('[data-testid="card-not-matching"]');
    expect(notMatchCard).toBeTruthy();
    expect(notMatchCard.textContent).toContain('150');
    expect(notMatchCard.textContent).toContain('350');
  });

  it('should display missing from DS2 count', () => {
    const missingDs2Card = fixture.nativeElement.querySelector('[data-testid="card-missing-ds2"]');
    expect(missingDs2Card).toBeTruthy();
    expect(missingDs2Card.textContent).toContain('50');
  });

  it('should display missing from DS1 count', () => {
    const missingDs1Card = fixture.nativeElement.querySelector('[data-testid="card-missing-ds1"]');
    expect(missingDs1Card).toBeTruthy();
    expect(missingDs1Card.textContent).toContain('84');
  });

  it('should handle zero/undefined counts gracefully', () => {
    const emptySummary: ComparisonSummary = {
      id: 'test-comp-empty',
      status: 'COMPLETED',
      createdAt: '2026-08-28T00:00:00Z'
    };
    fixture.componentRef.setInput('summary', emptySummary);
    fixture.detectChanges();

    const ds1Card = fixture.nativeElement.querySelector('[data-testid="card-ds1-total"]');
    expect(ds1Card.textContent).toContain('0');
  });
});
