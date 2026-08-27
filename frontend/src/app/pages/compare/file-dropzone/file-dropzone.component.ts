import { Component, ElementRef, ViewChild, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-file-dropzone',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <div
      class="dropzone-container"
      [class.dragover]="isDragOver()"
      [class.has-file]="!!selectedFile()"
      [class.disabled]="disabled()"
      (dragover)="onDragOver($event)"
      (dragleave)="onDragLeave($event)"
      (drop)="onDrop($event)"
      (click)="triggerBrowse()"
      (keydown.enter)="triggerBrowse()"
      (keydown.space)="triggerBrowse()"
      tabindex="0"
      role="button"
      [attr.aria-label]="label() || 'File dropzone'"
    >
      <input
        #fileInput
        type="file"
        class="file-input"
        [accept]="accept()"
        [disabled]="disabled()"
        (change)="onFileChange($event)"
      />

      @if (!selectedFile()) {
        <div class="dropzone-empty">
          <mat-icon class="dropzone-icon">cloud_upload</mat-icon>
          <div class="dropzone-text">
            <span class="primary-text">Drag and drop file here</span>
            <span class="secondary-text">or <span class="browse-link">browse</span> (.csv, .txt, .xls, .xlsx)</span>
          </div>
        </div>
      } @else {
        <div class="dropzone-file-info" (click)="$event.stopPropagation()">
          <mat-icon class="file-icon">{{ getFileIcon(selectedFile()!.name) }}</mat-icon>
          <div class="file-details">
            <span class="file-name" [title]="selectedFile()!.name">{{ selectedFile()!.name }}</span>
            <span class="file-size">{{ formatFileSize(selectedFile()!.size) }}</span>
          </div>
          <button
            mat-icon-button
            type="button"
            class="remove-file-btn"
            aria-label="Remove file"
            (click)="removeFile($event)"
            [disabled]="disabled()"
          >
            <mat-icon>close</mat-icon>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .dropzone-container {
      border: 2px dashed #cbd5e1;
      border-radius: 8px;
      padding: 24px 16px;
      text-align: center;
      background-color: #f8fafc;
      transition: all 0.2s ease-in-out;
      cursor: pointer;
      outline: none;
      min-height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover:not(.disabled):not(.has-file) {
        border-color: #3b82f6;
        background-color: #eff6ff;
      }

      &:focus-visible {
        border-color: #2563eb;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.2);
      }

      &.dragover {
        border-color: #2563eb;
        background-color: #dbeafe;
        transform: scale(1.01);
      }

      &.has-file {
        border-style: solid;
        border-color: #93c5fd;
        background-color: #f0f9ff;
        cursor: default;
      }

      &.disabled {
        opacity: 0.6;
        cursor: not-allowed;
        pointer-events: none;
      }
    }

    .file-input {
      display: none;
    }

    .dropzone-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
    }

    .dropzone-icon {
      font-size: 36px;
      width: 36px;
      height: 36px;
      color: #64748b;
    }

    .dropzone-text {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .primary-text {
        font-weight: 500;
        color: #334155;
      }

      .secondary-text {
        font-size: 0.875rem;
        color: #64748b;

        .browse-link {
          color: #2563eb;
          font-weight: 500;
          text-decoration: underline;
        }
      }
    }

    .dropzone-file-info {
      display: flex;
      align-items: center;
      width: 100%;
      gap: 12px;
      padding: 4px 8px;
    }

    .file-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: #2563eb;
    }

    .file-details {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      flex: 1;
      overflow: hidden;

      .file-name {
        font-weight: 500;
        color: #1e293b;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 100%;
      }

      .file-size {
        font-size: 0.75rem;
        color: #64748b;
      }
    }

    .remove-file-btn {
      color: #64748b;
      &:hover {
        color: #ef4444;
      }
    }
  `]
})
export class FileDropzoneComponent {
  accept = input<string>('.csv,.txt,.xls,.xlsx');
  label = input<string>('');
  disabled = input<boolean>(false);

  fileSelected = output<File>();
  fileRemoved = output<void>();

  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  isDragOver = signal<boolean>(false);
  selectedFile = signal<File | null>(null);

  onDragOver(event: DragEvent): void {
    if (this.disabled()) return;
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(true);
  }

  onDragLeave(event: DragEvent): void {
    if (this.disabled()) return;
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);
  }

  onDrop(event: DragEvent): void {
    if (this.disabled()) return;
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver.set(false);

    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      const file = event.dataTransfer.files[0];
      this.setFile(file);
    }
  }

  triggerBrowse(): void {
    if (this.disabled() || this.selectedFile()) return;
    if (this.fileInputRef) {
      this.fileInputRef.nativeElement.click();
    }
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.setFile(file);
    }
  }

  setFile(file: File): void {
    this.selectedFile.set(file);
    this.fileSelected.emit(file);
  }

  removeFile(event: MouseEvent): void {
    event.stopPropagation();
    this.selectedFile.set(null);
    if (this.fileInputRef) {
      this.fileInputRef.nativeElement.value = '';
    }
    this.fileRemoved.emit();
  }

  getFileIcon(fileName: string): string {
    const lower = fileName.toLowerCase();
    if (lower.endsWith('.csv') || lower.endsWith('.txt')) {
      return 'description';
    }
    if (lower.endsWith('.xls') || lower.endsWith('.xlsx')) {
      return 'table_chart';
    }
    return 'insert_drive_file';
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
