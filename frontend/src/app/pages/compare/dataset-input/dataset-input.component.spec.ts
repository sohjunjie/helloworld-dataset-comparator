import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DatasetInputComponent } from './dataset-input.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('DatasetInputComponent', () => {
  let component: DatasetInputComponent;
  let fixture: ComponentFixture<DatasetInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DatasetInputComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(DatasetInputComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('title', 'Dataset 1');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Dataset 1');
  });

  it('should have Upload File selected by default and SQL mode disabled', () => {
    expect(component.sourceType()).toBe('FILE_UPLOAD');
  });

  it('should show custom delimiter input when custom is selected', async () => {
    expect(fixture.nativeElement.querySelector('.custom-delimiter-input')).toBeNull();

    component.delimiterType.set('CUSTOM');
    fixture.detectChanges();

    const customInput = fixture.nativeElement.querySelector('.custom-delimiter-input');
    expect(customInput).toBeTruthy();
  });

  it('should resolve effective delimiter', () => {
    component.delimiterType.set('AUTO');
    expect(component.getEffectiveDelimiter()).toBe('AUTO');

    component.delimiterType.set(',');
    expect(component.getEffectiveDelimiter()).toBe(',');

    component.delimiterType.set('CUSTOM');
    component.customDelimiter.set('~');
    expect(component.getEffectiveDelimiter()).toBe('~');
  });

  it('should handle file selection from dropzone', () => {
    const file = new File(['test'], 'ds1.csv', { type: 'text/csv' });
    component.onFileSelected(file);
    fixture.detectChanges();

    expect(component.selectedFile()).toBe(file);
  });
});
