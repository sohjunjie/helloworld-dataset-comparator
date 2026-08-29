import { Component, ElementRef, ViewChild, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-file-dropzone',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './file-dropzone.component.html',
  styleUrl: './file-dropzone.component.scss'
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
