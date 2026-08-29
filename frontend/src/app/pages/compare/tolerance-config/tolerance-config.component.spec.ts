import '../../../../test-setup';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ToleranceConfigComponent } from './tolerance-config.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ToleranceConfig } from '../../../models/comparison.model';

describe('ToleranceConfigComponent', () => {
  let component: ToleranceConfigComponent;
  let fixture: ComponentFixture<ToleranceConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ToleranceConfigComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(ToleranceConfigComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('availableColumns', ['amount', 'price', 'quantity']);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty tolerances', () => {
    expect(component.tolerances().length).toBe(0);
  });

  it('should add a new tolerance row', () => {
    component.addTolerance();
    fixture.detectChanges();

    expect(component.tolerances().length).toBe(1);
    expect(component.tolerances()[0].columnName).toBe('');
    expect(component.tolerances()[0].percentage).toBe(1.0);
  });

  it('should remove a tolerance row and emit update', () => {
    component.addTolerance();
    component.updateColumn(0, 'amount');
    component.addTolerance();
    component.updateColumn(1, 'price');
    fixture.detectChanges();
    expect(component.tolerances().length).toBe(2);

    let emitted: ToleranceConfig[] = [];
    component.tolerancesChange.subscribe((t) => {
      emitted = t;
    });

    component.removeTolerance(0);
    fixture.detectChanges();

    expect(component.tolerances().length).toBe(1);
    expect(emitted.length).toBe(1);
    expect(emitted[0].columnName).toBe('price');
  });

  it('should update tolerance values and emit valid entries', () => {
    let emitted: ToleranceConfig[] = [];
    component.tolerancesChange.subscribe((t) => {
      emitted = t;
    });

    component.addTolerance();
    component.updateColumn(0, 'amount');
    component.updatePercentage(0, 5.5);
    fixture.detectChanges();

    expect(component.tolerances()[0].columnName).toBe('amount');
    expect(component.tolerances()[0].percentage).toBe(5.5);
    expect(emitted).toEqual([{ columnName: 'amount', percentage: 5.5 }]);
  });

  it('should validate percentage between 0 and 100 and show inline error when out of range', () => {
    component.addTolerance();
    component.updateColumn(0, 'amount');
    component.updatePercentage(0, 150);
    fixture.detectChanges();

    expect(component.isValid()).toBe(false);
    const errorEl = fixture.nativeElement.querySelector('.percentage-field mat-error');
    expect(errorEl).toBeTruthy();
    expect(errorEl.textContent).toContain('Percentage must be between 0 and 100');

    component.updatePercentage(0, -5);
    fixture.detectChanges();
    expect(component.isValid()).toBe(false);

    component.updatePercentage(0, 5);
    fixture.detectChanges();
    expect(component.isValid()).toBe(true);
    expect(fixture.nativeElement.querySelector('.percentage-field mat-error')).toBeNull();
  });

  it('should show inline error when column is empty', () => {
    component.addTolerance();
    component.updateColumn(0, '');
    component.updatePercentage(0, 2);
    fixture.detectChanges();

    expect(component.isValid()).toBe(false);
    const colError = fixture.nativeElement.querySelector('.column-field mat-error');
    expect(colError).toBeTruthy();
    expect(colError.textContent).toContain('Column is required');
  });
});

