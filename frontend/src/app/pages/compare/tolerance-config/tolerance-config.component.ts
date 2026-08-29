import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ErrorStateMatcher } from '@angular/material/core';
import { InstantErrorStateMatcher } from '../../../utils/instant-error-state-matcher';
import { ToleranceConfig } from '../../../models/comparison.model';

export interface ToleranceItem {
  columnName: string;
  percentage: number;
}

@Component({
  selector: 'app-tolerance-config',
  standalone: true,
  providers: [
    { provide: ErrorStateMatcher, useClass: InstantErrorStateMatcher }
  ],
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './tolerance-config.component.html',
  styleUrl: './tolerance-config.component.scss'
})
export class ToleranceConfigComponent {
  readonly errorMatcher = new InstantErrorStateMatcher();

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
    let num: number;
    if (typeof percentageVal === 'string') {
      num = percentageVal.trim() === '' ? NaN : parseFloat(percentageVal);
    } else {
      num = percentageVal;
    }

    const list = [...this.tolerances()];
    if (list[index]) {
      list[index] = { ...list[index], percentage: num };
      this.tolerances.set(list);
      this.emitChange();
    }
  }

  isInvalidPercentage(percentage: number | null | undefined): boolean {
    return percentage === null || percentage === undefined || isNaN(percentage) || percentage < 0 || percentage > 100;
  }

  private isItemValid(t: ToleranceItem): boolean {
    return (
      t.columnName.trim().length > 0 &&
      t.percentage !== null &&
      t.percentage !== undefined &&
      !isNaN(t.percentage) &&
      t.percentage >= 0 &&
      t.percentage <= 100
    );
  }

  isValid(): boolean {
    return this.tolerances().every(t => this.isItemValid(t));
  }

  private emitChange(): void {
    const validTolerances: ToleranceConfig[] = this.tolerances()
      .filter(t => this.isItemValid(t))
      .map(t => ({ columnName: t.columnName.trim(), percentage: t.percentage }));
    this.tolerancesChange.emit(validTolerances);
  }

}

