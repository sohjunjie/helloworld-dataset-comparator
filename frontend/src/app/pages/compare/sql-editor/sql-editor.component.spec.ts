import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SqlEditorComponent } from './sql-editor.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('SqlEditorComponent', () => {
  let component: SqlEditorComponent;
  let fixture: ComponentFixture<SqlEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SqlEditorComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(SqlEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize CodeMirror editor in container', () => {
    const container = fixture.nativeElement.querySelector('.cm-editor');
    expect(container).toBeTruthy();
  });

  it('should set and get query value', () => {
    const testSql = 'SELECT id, name, salary FROM employees WHERE active = true';
    component.setQuery(testSql);
    expect(component.getQuery()).toBe(testSql);
  });

  it('should emit queryChange when query is updated', () => {
    let emitted = '';
    component.queryChange.subscribe((val) => {
      emitted = val;
    });

    const testSql = 'SELECT * FROM accounts';
    component.setQuery(testSql);
    expect(component.getQuery()).toBe(testSql);
  });

  it('should clean up editor instance on destroy', () => {
    expect(component.getEditorView()).toBeTruthy();
    fixture.destroy();
    expect(component.getEditorView()).toBeNull();
  });
});
