import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FileDropzoneComponent } from './file-dropzone.component';

describe('FileDropzoneComponent', () => {
  let component: FileDropzoneComponent;
  let fixture: ComponentFixture<FileDropzoneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileDropzoneComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FileDropzoneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should highlight on dragover and unhighlight on dragleave', () => {
    const mockDragEvent = {
      preventDefault: () => {},
      stopPropagation: () => {}
    } as unknown as DragEvent;

    component.onDragOver(mockDragEvent);
    fixture.detectChanges();
    expect(component.isDragOver()).toBe(true);

    component.onDragLeave(mockDragEvent);
    fixture.detectChanges();
    expect(component.isDragOver()).toBe(false);
  });

  it('should select file on drop and emit fileSelected', () => {
    let emittedFile: File | null = null;
    component.fileSelected.subscribe((file) => {
      emittedFile = file;
    });

    const file = new File(['id,name\n1,Test'], 'test.csv', { type: 'text/csv' });
    const mockDropEvent = {
      preventDefault: () => {},
      stopPropagation: () => {},
      dataTransfer: {
        files: [file]
      }
    } as unknown as DragEvent;

    component.onDrop(mockDropEvent);
    fixture.detectChanges();

    expect(component.selectedFile()).toBe(file);
    expect(emittedFile).toBe(file);
    expect(fixture.nativeElement.textContent).toContain('test.csv');
  });

  it('should select file via setFile method and input change', () => {
    let emittedFile: File | null = null;
    component.fileSelected.subscribe((file) => {
      emittedFile = file;
    });

    const file = new File(['test'], 'data.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    
    const mockEvent = {
      target: {
        files: [file]
      }
    } as unknown as Event;

    component.onFileChange(mockEvent);
    fixture.detectChanges();

    expect(component.selectedFile()).toBe(file);
    expect(emittedFile).toBe(file);
    expect(fixture.nativeElement.textContent).toContain('data.xlsx');
  });

  it('should remove file and emit fileRemoved', () => {
    const file = new File(['test'], 'data.csv', { type: 'text/csv' });
    component.setFile(file);
    fixture.detectChanges();

    let removed = false;
    component.fileRemoved.subscribe(() => {
      removed = true;
    });

    const removeBtn = fixture.nativeElement.querySelector('.remove-file-btn') as HTMLButtonElement;
    expect(removeBtn).toBeTruthy();
    removeBtn.click();
    fixture.detectChanges();

    expect(component.selectedFile()).toBeNull();
    expect(removed).toBe(true);
  });
});
