import '../../../../test-setup';
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

  it('should have Upload File selected by default and SQL mode enabled', () => {
    expect(component.sourceType()).toBe('FILE_UPLOAD');
    expect(fixture.nativeElement.querySelector('app-file-dropzone')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-sql-editor')).toBeNull();
  });

  it('should toggle to SQL mode and reveal SQL editor + connection expansion panel', () => {
    component.setSourceType('SQL_QUERY');
    fixture.detectChanges();

    expect(component.sourceType()).toBe('SQL_QUERY');
    expect(fixture.nativeElement.querySelector('app-file-dropzone')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-sql-editor')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('mat-expansion-panel')).toBeTruthy();
  });

  it('should have connection fields with default host and port', () => {
    component.setSourceType('SQL_QUERY');
    fixture.detectChanges();

    expect(component.host()).toBe('localhost');
    expect(component.port()).toBe(5432);
    expect(component.database()).toBe('');
    expect(component.username()).toBe('');
    expect(component.password()).toBe('');
  });

  it('should validate SQL mode correctly when query and connection details are provided', () => {
    component.setSourceType('SQL_QUERY');
    fixture.detectChanges();

    expect(component.isValid()).toBe(false);

    component.onSqlQueryChanged('SELECT id, name FROM users');
    expect(component.isValid()).toBe(false);

    component.host.set('pg-host.example.com');
    component.port.set(5432);
    component.database.set('my_db');
    component.username.set('pg_user');
    component.password.set('secret123');

    expect(component.isValid()).toBe(true);

    const config = component.getConnectionConfig();
    expect(config).toEqual({
      host: 'pg-host.example.com',
      port: 5432,
      database: 'my_db',
      username: 'pg_user',
      password: 'secret123'
    });
  });

  it('should show custom delimiter input when custom is selected in file mode', () => {
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

  it('should handle file selection from dropzone and validate in file mode', () => {
    expect(component.isValid()).toBe(false);

    const file = new File(['test'], 'ds1.csv', { type: 'text/csv' });
    component.onFileSelected(file);
    fixture.detectChanges();

    expect(component.selectedFile()).toBe(file);
    expect(component.isValid()).toBe(true);
  });

  it('should display inline error when no file is selected in file mode', () => {
    expect(component.selectedFile()).toBeNull();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.field-error-hint mat-error');
    expect(errorEl).toBeTruthy();
    expect(errorEl.textContent).toContain('No file selected');
  });

  it('should validate custom delimiter and show inline errors for empty or multi-character input', () => {
    const file = new File(['test'], 'ds1.csv', { type: 'text/csv' });
    component.onFileSelected(file);
    component.delimiterType.set('CUSTOM');
    component.setCustomDelimiter('');
    fixture.detectChanges();

    expect(component.isCustomDelimiterValid()).toBe(false);
    expect(component.isValid()).toBe(false);

    let errorEl = fixture.nativeElement.querySelector('.custom-delimiter-input mat-error');
    expect(errorEl).toBeTruthy();
    expect(errorEl.textContent).toContain('Delimiter required');

    component.setCustomDelimiter('||');
    fixture.detectChanges();
    expect(component.isCustomDelimiterValid()).toBe(false);
    expect(component.isValid()).toBe(false);
    errorEl = fixture.nativeElement.querySelector('.custom-delimiter-input mat-error');
    expect(errorEl.textContent).toContain('Must be 1 char');

    component.setCustomDelimiter('|');
    fixture.detectChanges();
    expect(component.isCustomDelimiterValid()).toBe(true);
    expect(component.isValid()).toBe(true);
  });

  it('should display inline errors for missing SQL query and invalid port in SQL mode', () => {
    component.setSourceType('SQL_QUERY');
    component.onSqlQueryChanged('');
    component.setHost('localhost');
    component.setPort(70000); // Out of range
    component.setDatabase('');
    component.setUsername('');
    component.setPassword('');
    fixture.detectChanges();

    expect(component.isValid()).toBe(false);

    const sqlError = fixture.nativeElement.querySelector('.sql-error-hint mat-error');
    expect(sqlError).toBeTruthy();
    expect(sqlError.textContent).toContain('SQL query is required');

    const portError = fixture.nativeElement.querySelector('.port-field mat-error');
    expect(portError).toBeTruthy();
    expect(portError.textContent).toContain('Port must be between 1 and 65535');

    const dbError = fixture.nativeElement.querySelector('.db-field mat-error');
    expect(dbError).toBeTruthy();
    expect(dbError.textContent).toContain('Database is required');
  });
});


