import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ToleranceConfig } from '../../../models/comparison.model';

export interface ToleranceItem {
  columnName: string;
  percentage: number;
}

@Component({
  selector: 'app-tolerance-config',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="tolerance-container">
      <div class="tolerance-header">
        <div class="header-text">
          <span class="section-title">Numeric Tolerances (Optional)</span>
          <span class="section-desc">Allow near-matches on numerical columns within a percentage variance.</span>
        </div>
        <button
          mat-stroked-button
          type="button"
          class="add-tolerance-btn"
          (click)="addTolerance()"
          [disabled]="disabled()"
        >
          <mat-icon>add</mat-icon>
          Add Tolerance Column
        </button>
      </div>

      @if (tolerances().length > 0) {
        <div class="tolerance-list">
          @for (item of tolerances(); track $index; let idx = $index) {
            <div class="tolerance-row">
              <mat-form-field appearance="outline" class="column-field">
                <mat-label>Column</mat-label>
                <mat-select
                  [ngModel]="item.columnName"
                  (ngModelChange)="updateColumn(idx, $event)"
                  [disabled]="disabled()"
                  placeholder="Select numeric column"
                >
                  @for (col of availableColumns(); track col) {
                    <mat-option [value]="col">{{ col }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="percentage-field">
                <mat-label>Tolerance %</mat-label>
                <input
                  matInput
                  type="number"
                  min="0"
                  max="100"
                  step="0.1"
                  [ngModel]="item.percentage"
                  (ngModelChange)="updatePercentage(idx, $event)"
                  [disabled]="disabled()"
                />
                <span matTextSuffix>%</span>
              </mat-form-field>

              <button
                mat-icon-button
                type="button"
                color="warn"
                class="remove-btn"
                aria-label="Remove tolerance"
                (click)="removeTolerance(idx)"
                [disabled]="disabled()"
              >
                <mat-icon>delete</mat-icon>
              </button>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .tolerance-container {
      display: flex;
      flex-direction: column;
      gap: 16px;
      width: 100%;
    }

    .tolerance-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;
    }

    .header-text {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .section-title {
      font-size: 1rem;
      font-weight: 500;
      color: #1e293b;
    }

    .section-desc {
      font-size: 0.8125rem;
      color: #64748b;
    }

    .tolerance-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding: 12px;
      background-color: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
    }

    .tolerance-row {
      display: flex;
      gap: 12px;
      align-items: center;
    }

    .column-field {
      flex: 2;
      min-width: 180px;
    }

    .percentage-field {
      flex: 1;
      min-width: 120px;
      max-width: 160px;
    }

    .remove-btn {
      margin-bottom: 22px;
    }
  `]
})
export class ToleranceConfigComponent {
  availableColumns = input<string[]>([]);
  disabled = input<boolean>(false);

  tolerancesChange = output<ToleranceConfig[]>();

  tolerances = signal<ToleranceItem[]>([]);

  addTolerance(): void {
    const nextList: ToleranceItem[] = [
      ...this.tolerances(),
      { columnName: '', percentage: 1.0 }
    ];
    this.tolerances.set(nextList);
    this.emitChange();
  }

  removeTolerance(index: number): void {
    const nextList = this.tolerances().filter((_, i) => i !== index);
    this.tolerances.set(nextList);
    this.emitChange();
  }

  updateColumn(index: number, columnName: string): void {
    const list = [...this.tolerances()];
    if (list[index]) {
      list[index] = { ...list[index], columnName: columnName || '' };
      this.tolerances.set(list);
      this.emitChange();
    }
  }

  updatePercentage(index: number, percentageVal: number | string): void {
    let num = typeof percentageVal === 'string' ? parseFloat(percentageVal) : percentageVal;
    if (isNaN(num)) num = 0;
    if (num < 0) num = 0;
    if (num > 100) num = 100;

    const list = [...this.tolerances()];
    if (list[index]) {
      list[index] = { ...list[index], percentage: num };
      this.tolerances.set(list);
      this.emitChange();
    }
  }

  private emitChange(): void {
    const validTolerances: ToleranceConfig[] = this.tolerances()
      .filter(t => t.columnName.trim().length > 0)
      .map(t => ({ columnName: t.columnName.trim(), percentage: t.percentage }));
    this.tolerancesChange.emit(validTolerances);
  }
}
