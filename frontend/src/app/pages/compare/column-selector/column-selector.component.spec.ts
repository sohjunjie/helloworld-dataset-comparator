import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ColumnSelectorComponent } from './column-selector.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

describe('ColumnSelectorComponent', () => {
  let component: ColumnSelectorComponent;
  let fixture: ComponentFixture<ColumnSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ColumnSelectorComponent],
      providers: [provideAnimationsAsync()]
    }).compileComponents();

    fixture = TestBed.createComponent(ColumnSelectorComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('availableColumns', ['id', 'user_id', 'email', 'name']);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should allow selecting multiple columns from available list', () => {
    let emittedKeys: string[] = [];
    component.selectedKeysChange.subscribe((keys) => {
      emittedKeys = keys;
    });

    component.onSelectChange(['id', 'email']);
    fixture.detectChanges();

    expect(component.selectedKeys()).toEqual(['id', 'email']);
    expect(emittedKeys).toEqual(['id', 'email']);
  });

  it('should allow adding manual column names', () => {
    let emittedKeys: string[] = [];
    component.selectedKeysChange.subscribe((keys) => {
      emittedKeys = keys;
    });

    component.manualColumnInput.set('custom_key');
    component.addManualColumn();
    fixture.detectChanges();

    expect(component.selectedKeys()).toContain('custom_key');
    expect(emittedKeys).toContain('custom_key');
    expect(component.manualColumnInput()).toBe('');
  });

  it('should not add duplicate or empty manual column names', () => {
    component.selectedKeys.set(['id']);
    component.manualColumnInput.set('   ');
    component.addManualColumn();
    expect(component.selectedKeys()).toEqual(['id']);

    component.manualColumnInput.set('id');
    component.addManualColumn();
    expect(component.selectedKeys()).toEqual(['id']);
  });

  it('should remove a selected key', () => {
    component.selectedKeys.set(['id', 'email']);
    component.removeKey('id');
    fixture.detectChanges();

    expect(component.selectedKeys()).toEqual(['email']);
  });
});
