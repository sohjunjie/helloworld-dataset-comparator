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
import { ErrorStateMatcher } from '@angular/material/core';
import { InstantErrorStateMatcher } from '../../../utils/instant-error-state-matcher';

export interface DelimiterOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-dataset-input',
  standalone: true,
  providers: [
    { provide: ErrorStateMatcher, useClass: InstantErrorStateMatcher }
  ],
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
  templateUrl: './dataset-input.component.html',
  styleUrl: './dataset-input.component.scss'
})
export class DatasetInputComponent {
  @ViewChild('sqlEditor') sqlEditor?: SqlEditorComponent;

  readonly errorMatcher = new InstantErrorStateMatcher();

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

  setCustomDelimiter(delim: string): void {
    this.customDelimiterModel = delim;
    this.customDelimiter.set(delim);
    this.onStateChanged();
  }

  setPort(portNum: number): void {
    this.portModel = portNum;
    this.port.set(portNum);
    this.onStateChanged();
  }

  setHost(hostStr: string): void {
    this.hostModel = hostStr;
    this.host.set(hostStr);
    this.onStateChanged();
  }

  setDatabase(db: string): void {
    this.databaseModel = db;
    this.database.set(db);
    this.onStateChanged();
  }

  setUsername(user: string): void {
    this.usernameModel = user;
    this.username.set(user);
    this.onStateChanged();
  }

  setPassword(pwd: string): void {
    this.passwordModel = pwd;
    this.password.set(pwd);
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

  isCustomDelimiterValid(): boolean {
    return this.customDelimiter().length === 1 && this.customDelimiter().trim().length > 0;
  }

  isConnectionValid(): boolean {
    const p = Number(this.port());
    return (
      !!this.host()?.trim() &&
      !isNaN(p) &&
      p >= 1 &&
      p <= 65535 &&
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
      const hasValidFile = !!this.selectedFile();
      const hasValidDelimiter = this.delimiterType() !== 'CUSTOM' || this.isCustomDelimiterValid();
      return hasValidFile && hasValidDelimiter;
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
