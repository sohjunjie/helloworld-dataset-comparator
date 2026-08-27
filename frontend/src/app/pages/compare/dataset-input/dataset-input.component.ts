import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { FileDropzoneComponent } from '../file-dropzone/file-dropzone.component';
import { DataSourceType } from '../../../models/comparison.model';

export interface DelimiterOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-dataset-input',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatRadioModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    FileDropzoneComponent
  ],
  template: `
    <mat-card class="dataset-card">
      <mat-card-header>
        <mat-card-title class="card-title">
          <mat-icon class="title-icon">dataset</mat-icon>
          <span>{{ title() }}</span>
        </mat-card-title>
      </mat-card-header>

      <mat-card-content class="card-body">
        <div class="source-type-toggle">
          <mat-radio-group
            [(ngModel)]="sourceTypeModel"
            (ngModelChange)="sourceType.set($event)"
            [disabled]="disabled()"
            class="radio-group"
          >
            <mat-radio-button value="FILE_UPLOAD">Upload File</mat-radio-button>
            <mat-radio-button value="SQL_QUERY" [disabled]="true">
              SQL Query <span class="badge-soon">(Coming soon)</span>
            </mat-radio-button>
          </mat-radio-group>
        </div>

        @if (sourceType() === 'FILE_UPLOAD') {
          <div class="file-upload-section">
            <app-file-dropzone
              [label]="title() + ' File Upload'"
              [disabled]="disabled()"
              (fileSelected)="onFileSelected($event)"
              (fileRemoved)="onFileRemoved()"
            ></app-file-dropzone>

            <div class="delimiter-row">
              <mat-form-field appearance="outline" class="delimiter-select">
                <mat-label>Delimiter</mat-label>
                <mat-select
                  [(ngModel)]="delimiterModel"
                  (ngModelChange)="delimiterType.set($event)"
                  [disabled]="disabled()"
                >
                  @for (opt of delimiterOptions; track opt.value) {
                    <mat-option [value]="opt.value">{{ opt.label }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>

              @if (delimiterType() === 'CUSTOM') {
                <mat-form-field appearance="outline" class="custom-delimiter-input">
                  <mat-label>Custom Char</mat-label>
                  <input
                    matInput
                    [(ngModel)]="customDelimiterModel"
                    (ngModelChange)="customDelimiter.set($event)"
                    maxlength="1"
                    placeholder="e.g. ^"
                    [disabled]="disabled()"
                  />
                  <mat-hint>1 char</mat-hint>
                </mat-form-field>
              }
            </div>
          </div>
        }
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .dataset-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      background-color: #ffffff;
    }

    mat-card-header {
      padding: 16px 20px 8px;
    }

    .card-title {
      font-size: 1.125rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #0f172a;
    }

    .title-icon {
      color: #3b82f6;
    }

    .card-body {
      padding: 8px 20px 20px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      flex: 1;
    }

    .source-type-toggle {
      margin-bottom: 4px;
    }

    .radio-group {
      display: flex;
      gap: 16px;
    }

    .badge-soon {
      font-size: 0.75rem;
      color: #94a3b8;
      font-weight: normal;
    }

    .file-upload-section {
      display: flex;
      flex-direction: column;
      gap: 16px;
      flex: 1;
    }

    .delimiter-row {
      display: flex;
      gap: 12px;
      align-items: flex-start;
      margin-top: 8px;
    }

    .delimiter-select {
      flex: 1;
    }

    .custom-delimiter-input {
      width: 120px;
    }
  `]
})
export class DatasetInputComponent {
  title = input<string>('Dataset');
  disabled = input<boolean>(false);

  fileChanged = output<File | null>();

  sourceTypeModel: DataSourceType = 'FILE_UPLOAD';
  sourceType = signal<DataSourceType>('FILE_UPLOAD');

  delimiterModel = 'AUTO';
  delimiterType = signal<string>('AUTO');

  customDelimiterModel = '';
  customDelimiter = signal<string>('');

  selectedFile = signal<File | null>(null);

  readonly delimiterOptions: DelimiterOption[] = [
    { value: 'AUTO', label: 'Auto-detect' },
    { value: ',', label: 'Comma (,)' },
    { value: '\t', label: 'Tab' },
    { value: '|', label: 'Pipe (|)' },
    { value: ';', label: 'Semicolon (;)' },
    { value: 'CUSTOM', label: 'Custom Delimiter' }
  ];

  onFileSelected(file: File): void {
    this.selectedFile.set(file);
    this.fileChanged.emit(file);
  }

  onFileRemoved(): void {
    this.selectedFile.set(null);
    this.fileChanged.emit(null);
  }

  getEffectiveDelimiter(): string {
    const type = this.delimiterType();
    if (type === 'CUSTOM') {
      return this.customDelimiter().trim() || 'AUTO';
    }
    return type;
  }
}
