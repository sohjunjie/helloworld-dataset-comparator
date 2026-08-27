import { Component, ViewChild, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { FileDropzoneComponent } from '../file-dropzone/file-dropzone.component';
import { SqlEditorComponent } from '../sql-editor/sql-editor.component';
import { DataSourceType, DatabaseConnectionConfig } from '../../../models/comparison.model';

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
    MatExpansionModule,
    FileDropzoneComponent,
    SqlEditorComponent
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
            (ngModelChange)="onSourceTypeChanged($event)"
            [disabled]="disabled()"
            class="radio-group"
          >
            <mat-radio-button value="FILE_UPLOAD">Upload File</mat-radio-button>
            <mat-radio-button value="SQL_QUERY">SQL Query</mat-radio-button>
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
                  (ngModelChange)="delimiterType.set($event); onStateChanged()"
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
                    (ngModelChange)="customDelimiter.set($event); onStateChanged()"
                    maxlength="1"
                    placeholder="e.g. ^"
                    [disabled]="disabled()"
                  />
                  <mat-hint>1 char</mat-hint>
                </mat-form-field>
              }
            </div>
          </div>
        } @else {
          <div class="sql-query-section">
            <app-sql-editor
              #sqlEditor
              [disabled]="disabled()"
              [query]="sqlQuery()"
              (queryChange)="onSqlQueryChanged($event)"
            ></app-sql-editor>

            <mat-expansion-panel [expanded]="true" class="connection-panel">
              <mat-expansion-panel-header>
                <mat-panel-title class="panel-title">
                  <mat-icon class="panel-icon">dns</mat-icon>
                  <span>PostgreSQL Connection Details</span>
                </mat-panel-title>
                <mat-panel-description>
                  {{ isConnectionValid() ? 'Configured' : 'Required' }}
                </mat-panel-description>
              </mat-expansion-panel-header>

              <div class="connection-form-grid">
                <mat-form-field appearance="outline" class="form-field host-field">
                  <mat-label>Host</mat-label>
                  <input
                    matInput
                    [(ngModel)]="hostModel"
                    (ngModelChange)="host.set($event); onStateChanged()"
                    [disabled]="disabled()"
                    required
                    placeholder="localhost"
                  />
                  @if (!host().trim()) {
                    <mat-error>Host is required</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline" class="form-field port-field">
                  <mat-label>Port</mat-label>
                  <input
                    matInput
                    type="number"
                    [(ngModel)]="portModel"
                    (ngModelChange)="port.set($event); onStateChanged()"
                    [disabled]="disabled()"
                    required
                    min="1"
                    max="65535"
                    placeholder="5432"
                  />
                  @if (!port() || port() <= 0) {
                    <mat-error>Valid port is required</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline" class="form-field db-field">
                  <mat-label>Database</mat-label>
                  <input
                    matInput
                    [(ngModel)]="databaseModel"
                    (ngModelChange)="database.set($event); onStateChanged()"
                    [disabled]="disabled()"
                    required
                    placeholder="e.g. comparator_db"
                  />
                  @if (!database().trim()) {
                    <mat-error>Database is required</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline" class="form-field user-field">
                  <mat-label>Username</mat-label>
                  <input
                    matInput
                    [(ngModel)]="usernameModel"
                    (ngModelChange)="username.set($event); onStateChanged()"
                    [disabled]="disabled()"
                    required
                    placeholder="e.g. postgres"
                  />
                  @if (!username().trim()) {
                    <mat-error>Username is required</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline" class="form-field pass-field">
                  <mat-label>Password</mat-label>
                  <input
                    matInput
                    type="password"
                    [(ngModel)]="passwordModel"
                    (ngModelChange)="password.set($event); onStateChanged()"
                    [disabled]="disabled()"
                    required
                    placeholder="••••••••"
                  />
                  @if (!password()) {
                    <mat-error>Password is required</mat-error>
                  }
                </mat-form-field>
              </div>
            </mat-expansion-panel>
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

    .file-upload-section, .sql-query-section {
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
      background-color: #ffffff;
      border-radius: 4px;

      ::ng-deep .mat-mdc-text-field-wrapper {
        background-color: #ffffff;
      }
    }

    .custom-delimiter-input {
      width: 120px;
      background-color: #ffffff;
      border-radius: 4px;

      ::ng-deep .mat-mdc-text-field-wrapper {
        background-color: #ffffff;
      }
    }

    .connection-panel {
      border: 1px solid #e2e8f0;
      border-radius: 8px !important;
      box-shadow: none !important;
      background-color: #f8fafc;
    }

    .panel-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.875rem;
      font-weight: 600;
      color: #334155;
    }

    .panel-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: #64748b;
    }

    .connection-form-grid {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 12px;
      padding-top: 8px;

      .db-field {
        grid-column: 1 / -1;
      }

      .user-field, .pass-field {
        grid-column: span 1;
      }

      @media (max-width: 600px) {
        grid-template-columns: 1fr;
        .user-field, .pass-field {
          grid-column: 1 / -1;
        }
      }
    }

    .form-field {
      width: 100%;
      background-color: #ffffff;
      border-radius: 4px;

      ::ng-deep .mat-mdc-text-field-wrapper {
        background-color: #ffffff;
      }
    }
  `]
})
export class DatasetInputComponent {
  @ViewChild('sqlEditor') sqlEditor?: SqlEditorComponent;

  title = input<string>('Dataset');
  disabled = input<boolean>(false);

  fileChanged = output<File | null>();
  stateChanged = output<void>();

  sourceTypeModel: DataSourceType = 'FILE_UPLOAD';
  sourceType = signal<DataSourceType>('FILE_UPLOAD');

  delimiterModel = 'AUTO';
  delimiterType = signal<string>('AUTO');

  customDelimiterModel = '';
  customDelimiter = signal<string>('');

  selectedFile = signal<File | null>(null);

  sqlQuery = signal<string>('');

  hostModel = 'localhost';
  host = signal<string>('localhost');

  portModel = 5432;
  port = signal<number>(5432);

  databaseModel = '';
  database = signal<string>('');

  usernameModel = '';
  username = signal<string>('');

  passwordModel = '';
  password = signal<string>('');

  readonly delimiterOptions: DelimiterOption[] = [
    { value: 'AUTO', label: 'Auto-detect' },
    { value: ',', label: 'Comma (,)' },
    { value: '\t', label: 'Tab' },
    { value: '|', label: 'Pipe (|)' },
    { value: ';', label: 'Semicolon (;)' },
    { value: 'CUSTOM', label: 'Custom Delimiter' }
  ];

  setSourceType(type: DataSourceType): void {
    this.sourceTypeModel = type;
    this.sourceType.set(type);
    this.onStateChanged();
  }

  onSourceTypeChanged(type: DataSourceType): void {
    this.sourceType.set(type);
    this.onStateChanged();
  }

  onFileSelected(file: File): void {
    this.selectedFile.set(file);
    this.fileChanged.emit(file);
    this.onStateChanged();
  }

  onFileRemoved(): void {
    this.selectedFile.set(null);
    this.fileChanged.emit(null);
    this.onStateChanged();
  }

  onSqlQueryChanged(sqlText: string): void {
    this.sqlQuery.set(sqlText);
    this.onStateChanged();
  }

  onStateChanged(): void {
    this.stateChanged.emit();
  }

  getEffectiveDelimiter(): string {
    const type = this.delimiterType();
    if (type === 'CUSTOM') {
      return this.customDelimiter().trim() || 'AUTO';
    }
    return type;
  }

  isConnectionValid(): boolean {
    return (
      !!this.host()?.trim() &&
      !!this.port() &&
      this.port() > 0 &&
      this.port() <= 65535 &&
      !!this.database()?.trim() &&
      !!this.username()?.trim() &&
      !!this.password()
    );
  }

  isSqlValid(): boolean {
    return !!this.sqlQuery()?.trim() && this.isConnectionValid();
  }

  isValid(): boolean {
    if (this.sourceType() === 'FILE_UPLOAD') {
      return !!this.selectedFile();
    }
    return this.isSqlValid();
  }

  getConnectionConfig(): DatabaseConnectionConfig {
    return {
      host: this.host().trim(),
      port: Number(this.port()),
      database: this.database().trim(),
      username: this.username().trim(),
      password: this.password()
    };
  }

  getSqlQuery(): string {
    return this.sqlQuery().trim();
  }
}
