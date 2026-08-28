import { Component, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { DatasetInputComponent } from './dataset-input/dataset-input.component';
import { ColumnSelectorComponent } from './column-selector/column-selector.component';
import { ToleranceConfigComponent } from './tolerance-config/tolerance-config.component';
import { ComparisonService } from '../../services/comparison.service';
import { ProgressService } from '../../services/progress.service';
import { ToleranceConfig, UploadDatasetOptions } from '../../models/comparison.model';

@Component({
  selector: 'app-compare',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatDividerModule,
    DatasetInputComponent,
    ColumnSelectorComponent,
    ToleranceConfigComponent
  ],
  templateUrl: './compare.component.html',
  styleUrl: './compare.component.scss'
})
export class CompareComponent {
  private readonly comparisonService = inject(ComparisonService);
  private readonly progressService = inject(ProgressService);
  private readonly router = inject(Router);

  @ViewChild('ds1Input') ds1InputComponent?: DatasetInputComponent;
  @ViewChild('ds2Input') ds2InputComponent?: DatasetInputComponent;
  @ViewChild(ColumnSelectorComponent) columnSelectorComponent?: ColumnSelectorComponent;
  @ViewChild(ToleranceConfigComponent) toleranceConfigComponent?: ToleranceConfigComponent;

  currentStep = signal<number>(1);
  comparisonId = signal<string | null>(null);

  ds1File = signal<File | null>(null);
  ds2File = signal<File | null>(null);

  availableColumns = signal<string[]>([]);
  selectedKeyColumns = signal<string[]>([]);
  tolerances = signal<ToleranceConfig[]>([]);

  caseSensitiveModel = true;
  caseSensitive = signal<boolean>(true);

  isUploading = signal<boolean>(false);
  isComparing = signal<boolean>(false);

  progressStage = signal<string>('Preparing comparison...');
  progressPercent = signal<number>(0);

  errorMessage = signal<string | null>(null);

  onDs1FileChanged(file: File | null): void {
    this.ds1File.set(file);
  }

  onDs2FileChanged(file: File | null): void {
    this.ds2File.set(file);
  }

  onDatasetStateChanged(): void {
    // Triggers change detection refresh for canUpload()
  }

  canUpload(): boolean {
    const ds1Valid = this.ds1InputComponent ? this.ds1InputComponent.isValid() : !!this.ds1File();
    const ds2Valid = this.ds2InputComponent ? this.ds2InputComponent.isValid() : !!this.ds2File();
    return ds1Valid && ds2Valid;
  }

  uploadFiles(): void {
    if (!this.canUpload()) return;

    this.isUploading.set(true);
    this.errorMessage.set(null);

    const ds1 = this.ds1InputComponent;
    const ds2 = this.ds2InputComponent;

    const ds1Type = ds1 ? ds1.sourceType() : 'FILE_UPLOAD';
    const ds2Type = ds2 ? ds2.sourceType() : 'FILE_UPLOAD';

    const uploadOptions: UploadDatasetOptions = {
      ds1File: ds1Type === 'FILE_UPLOAD' ? ds1?.selectedFile() || this.ds1File() : null,
      ds2File: ds2Type === 'FILE_UPLOAD' ? ds2?.selectedFile() || this.ds2File() : null,
      ds1Delimiter: ds1Type === 'FILE_UPLOAD' ? ds1?.getEffectiveDelimiter() || 'AUTO' : undefined,
      ds2Delimiter: ds2Type === 'FILE_UPLOAD' ? ds2?.getEffectiveDelimiter() || 'AUTO' : undefined,
      ds1Sql: ds1Type === 'SQL_QUERY' ? ds1?.getSqlQuery() : undefined,
      ds1Connection: ds1Type === 'SQL_QUERY' ? ds1?.getConnectionConfig() : undefined,
      ds2Sql: ds2Type === 'SQL_QUERY' ? ds2?.getSqlQuery() : undefined,
      ds2Connection: ds2Type === 'SQL_QUERY' ? ds2?.getConnectionConfig() : undefined
    };

    this.comparisonService.upload(uploadOptions).subscribe({
      next: (res) => {
        this.comparisonId.set(res.comparisonId);

        // Compute unique columns from both datasets
        const combined = Array.from(
          new Set([...(res.columns.ds1 || []), ...(res.columns.ds2 || [])])
        );
        this.availableColumns.set(combined);

        this.currentStep.set(2);
        this.isUploading.set(false);
      },
      error: (err) => {
        this.isUploading.set(false);
        this.errorMessage.set(
          err?.error?.message || err?.message || 'Failed to upload and analyze datasets. Please verify your inputs.'
        );
      }
    });
  }

  onKeyColumnsChanged(keys: string[]): void {
    this.selectedKeyColumns.set(keys);
  }

  onTolerancesChanged(tolerances: ToleranceConfig[]): void {
    this.tolerances.set(tolerances);
  }

  canCompare(): boolean {
    const hasKeys = this.selectedKeyColumns().length > 0;
    const tolerancesValid = this.toleranceConfigComponent ? this.toleranceConfigComponent.isValid() : true;
    return hasKeys && tolerancesValid;
  }


  startComparison(): void {
    const id = this.comparisonId();
    if (!id || !this.canCompare()) return;

    this.isComparing.set(true);
    this.errorMessage.set(null);
    this.progressStage.set('Initializing comparison...');
    this.progressPercent.set(5);

    let hasNavigated = false;
    const navigateOnce = () => {
      if (!hasNavigated) {
        hasNavigated = true;
        this.isComparing.set(false);
        this.router.navigate(['/results', id]);
      }
    };

    // Subscribe to SSE progress
    this.progressService.subscribe(id).subscribe({
      next: (update) => {
        this.progressStage.set(update.stage + (update.message ? `: ${update.message}` : ''));
        this.progressPercent.set(update.percent || 50);

        if (update.stage === 'COMPLETED') {
          navigateOnce();
        }
      },
      error: (err) => {
        logOrHandleError(err);
      }
    });

    // Fire execute POST
    this.comparisonService
      .execute(id, {
        keyColumns: this.selectedKeyColumns(),
        tolerances: this.tolerances(),
        caseSensitive: this.caseSensitive()
      })
      .subscribe({
        next: (summary) => {
          if (summary.status === 'COMPLETED') {
            navigateOnce();
          }
        },
        error: (err) => {
          this.isComparing.set(false);
          this.errorMessage.set(
            err?.error?.message || err?.message || 'Comparison execution failed. Please check your inputs.'
          );
        }
      });
  }

  resetForm(): void {
    this.currentStep.set(1);
    this.comparisonId.set(null);
    this.availableColumns.set([]);
    this.selectedKeyColumns.set([]);
    this.tolerances.set([]);
    this.isUploading.set(false);
    this.isComparing.set(false);
    this.errorMessage.set(null);
  }
}

function logOrHandleError(err: unknown): void {
  console.warn('Progress SSE event notification:', err);
}
