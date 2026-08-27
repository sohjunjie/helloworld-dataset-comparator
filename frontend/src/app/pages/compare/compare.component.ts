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
import { ToleranceConfig } from '../../models/comparison.model';

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
  template: `
    <div class="compare-page-container">
      <!-- Header -->
      <div class="page-header">
        <div class="header-info">
          <h1 class="page-title">New Dataset Comparison</h1>
          <p class="page-subtitle">
            Upload two datasets (CSV, TXT, Excel) to compare records, detect differences, and analyze missing data.
          </p>
        </div>
        @if (currentStep() === 2) {
          <button mat-button color="warn" (click)="resetForm()" [disabled]="isComparing()">
            <mat-icon>restart_alt</mat-icon>
            Start Over
          </button>
        }
      </div>

      <!-- Error Alert -->
      @if (errorMessage()) {
        <div class="error-banner" role="alert">
          <mat-icon class="error-icon">error_outline</mat-icon>
          <span class="error-text">{{ errorMessage() }}</span>
          <button mat-icon-button (click)="errorMessage.set(null)" aria-label="Dismiss error">
            <mat-icon>close</mat-icon>
          </button>
        </div>
      }

      <!-- Step 1: Dataset Inputs -->
      <div class="step-card" [class.completed-step]="currentStep() === 2">
        <div class="step-header">
          <span class="step-badge">1</span>
          <div class="step-heading">
            <h2>Select Datasets</h2>
            <p>Choose source files and configure delimiters for both datasets.</p>
          </div>
        </div>

        <div class="datasets-grid">
          <app-dataset-input
            #ds1Input
            title="Dataset 1 (Primary / Source)"
            [disabled]="isUploading() || isComparing() || currentStep() === 2"
            (fileChanged)="onDs1FileChanged($event)"
          ></app-dataset-input>

          <app-dataset-input
            #ds2Input
            title="Dataset 2 (Target / Comparison)"
            [disabled]="isUploading() || isComparing() || currentStep() === 2"
            (fileChanged)="onDs2FileChanged($event)"
          ></app-dataset-input>
        </div>

        @if (currentStep() === 1) {
          <div class="step-actions">
            <button
              mat-flat-button
              color="primary"
              class="upload-btn"
              [disabled]="!canUpload() || isUploading()"
              (click)="uploadFiles()"
            >
              @if (isUploading()) {
                <mat-spinner diameter="20" class="btn-spinner"></mat-spinner>
                Analyzing Datasets...
              } @else {
                <ng-container>
                  <mat-icon>upload_file</mat-icon>
                  Upload & Analyze Files
                </ng-container>
              }
            </button>
          </div>
        }
      </div>

      <!-- Step 2: Configuration & Execution -->
      @if (currentStep() === 2) {
        <div class="step-card active-step">
          <div class="step-header">
            <span class="step-badge">2</span>
            <div class="step-heading">
              <h2>Configure Comparison & Execute</h2>
              <p>Select the unique key column(s) to match records on and configure matching tolerances.</p>
            </div>
          </div>

          <div class="config-sections">
            <!-- Key Column Selection -->
            <div class="config-item">
              <app-column-selector
                [availableColumns]="availableColumns()"
                [disabled]="isComparing()"
                (selectedKeysChange)="onKeyColumnsChanged($event)"
              ></app-column-selector>
            </div>

            <mat-divider></mat-divider>

            <!-- Tolerance Configuration -->
            <div class="config-item">
              <app-tolerance-config
                [availableColumns]="availableColumns()"
                [disabled]="isComparing()"
                (tolerancesChange)="onTolerancesChanged($event)"
              ></app-tolerance-config>
            </div>

            <mat-divider></mat-divider>

            <!-- Options -->
            <div class="config-item options-row">
              <mat-checkbox
                [(ngModel)]="caseSensitiveModel"
                (ngModelChange)="caseSensitive.set($event)"
                [disabled]="isComparing()"
                color="primary"
              >
                <span class="checkbox-label">Case-sensitive string comparison</span>
              </mat-checkbox>
            </div>

            <!-- Progress Bar during Execution -->
            @if (isComparing()) {
              <div class="progress-section">
                <div class="progress-label-row">
                  <span class="progress-stage">{{ progressStage() }}</span>
                  <span class="progress-percent">{{ progressPercent() }}%</span>
                </div>
                <mat-progress-bar
                  [mode]="progressPercent() > 0 ? 'determinate' : 'indeterminate'"
                  [value]="progressPercent()"
                ></mat-progress-bar>
                <span class="progress-hint">Comparing datasets via embedded engine...</span>
              </div>
            }

            <!-- Execution Action -->
            <div class="step-actions">
              <button
                mat-flat-button
                color="primary"
                class="compare-btn"
                [disabled]="!canCompare() || isComparing()"
                (click)="startComparison()"
              >
                @if (isComparing()) {
                  <mat-spinner diameter="20" class="btn-spinner"></mat-spinner>
                  Running Comparison...
                } @else {
                  <ng-container>
                    <mat-icon>play_arrow</mat-icon>
                    Start Comparison
                  </ng-container>
                }
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .compare-page-container {
      max-width: 1100px;
      margin: 0 auto;
      padding: 32px 24px;
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 16px;
    }

    .page-title {
      font-size: 1.75rem;
      font-weight: 700;
      color: #0f172a;
      margin: 0 0 6px 0;
    }

    .page-subtitle {
      font-size: 0.9375rem;
      color: #64748b;
      margin: 0;
    }

    .error-banner {
      display: flex;
      align-items: center;
      gap: 12px;
      background-color: #fef2f2;
      border: 1px solid #fecaca;
      border-radius: 8px;
      padding: 12px 16px;
      color: #b91c1c;

      .error-icon {
        color: #ef4444;
      }

      .error-text {
        flex: 1;
        font-size: 0.875rem;
        font-weight: 500;
      }
    }

    .step-card {
      background-color: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      display: flex;
      flex-direction: column;
      gap: 20px;
      transition: all 0.2s ease;

      &.completed-step {
        background-color: #f8fafc;
        border-color: #e2e8f0;
      }

      &.active-step {
        border-color: #3b82f6;
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
      }
    }

    .step-header {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .step-badge {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background-color: #3b82f6;
      color: #ffffff;
      font-weight: 600;
      font-size: 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .step-heading {
      h2 {
        font-size: 1.25rem;
        font-weight: 600;
        color: #0f172a;
        margin: 0 0 2px 0;
      }
      p {
        font-size: 0.875rem;
        color: #64748b;
        margin: 0;
      }
    }

    .datasets-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;

      @media (max-width: 768px) {
        grid-template-columns: 1fr;
      }
    }

    .config-sections {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .config-item {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .options-row {
      padding: 4px 0;
    }

    .checkbox-label {
      font-weight: 500;
      color: #334155;
    }

    .step-actions {
      display: flex;
      justify-content: flex-end;
      padding-top: 8px;
    }

    .upload-btn, .compare-btn {
      padding: 0 24px;
      height: 48px;
      font-size: 0.9375rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      border-radius: 8px;
    }

    .btn-spinner {
      margin-right: 4px;
    }

    .progress-section {
      background-color: #f1f5f9;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      padding: 16px 20px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .progress-label-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.875rem;
      font-weight: 600;
      color: #1e293b;
    }

    .progress-hint {
      font-size: 0.75rem;
      color: #64748b;
    }
  `]
})
export class CompareComponent {
  private readonly comparisonService = inject(ComparisonService);
  private readonly progressService = inject(ProgressService);
  private readonly router = inject(Router);

  @ViewChild('ds1Input') ds1InputComponent?: DatasetInputComponent;
  @ViewChild('ds2Input') ds2InputComponent?: DatasetInputComponent;

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

  canUpload(): boolean {
    return !!this.ds1File() && !!this.ds2File();
  }

  uploadFiles(): void {
    const file1 = this.ds1File();
    const file2 = this.ds2File();
    if (!file1 || !file2) return;

    this.isUploading.set(true);
    this.errorMessage.set(null);

    const ds1Delim = this.ds1InputComponent?.getEffectiveDelimiter() || 'AUTO';
    const ds2Delim = this.ds2InputComponent?.getEffectiveDelimiter() || 'AUTO';

    this.comparisonService.upload(file1, file2, ds1Delim, ds2Delim).subscribe({
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
          err?.error?.message || err?.message || 'Failed to upload and analyze datasets. Please verify the files.'
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
    return this.selectedKeyColumns().length > 0;
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
