import {
  Component,
  OnInit,
  OnChanges,
  SimpleChanges,
  inject,
  input,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ComparisonService } from '../../../services/comparison.service';
import { MismatchDetail, MissingDetail } from '../../../models/comparison.model';

export type ResultTableType = 'mismatches' | 'missing_from_ds2' | 'missing_from_ds1';

@Component({
  selector: 'app-detail-table',
  standalone: true,
  imports: [
    CommonModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ],
  templateUrl: './detail-table.component.html',
  styleUrl: './detail-table.component.scss'
})
export class DetailTableComponent implements OnInit, OnChanges {
  private readonly comparisonService = inject(ComparisonService);

  comparisonId = input.required<string>();
  resultType = input.required<ResultTableType>();

  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  mismatchRows = signal<MismatchDetail[]>([]);
  missingRows = signal<MissingDetail[]>([]);

  ds1Columns = signal<string[]>([]);
  ds2Columns = signal<string[]>([]);
  missingColumns = signal<string[]>([]);

  pageIndex = signal<number>(0);
  pageSize = signal<number>(50);
  totalElements = signal<number>(0);

  ngOnInit(): void {
    this.resolveSchemaHeaders();
    this.loadData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['comparisonId'] && !changes['comparisonId'].firstChange) {
      this.pageIndex.set(0);
      this.resolveSchemaHeaders();
      this.loadData();
    } else if (changes['resultType'] && !changes['resultType'].firstChange) {
      this.pageIndex.set(0);
      this.loadData();
    }
  }

  private resolveSchemaHeaders(): void {
    const id = this.comparisonId();
    if (!id || !this.comparisonService?.getHeaders) return;
    this.comparisonService.getHeaders(id).subscribe({
      next: (headers) => {
        if (headers?.ds1?.length && !this.ds1Columns().length) {
          this.ds1Columns.set(headers.ds1);
        }
        if (headers?.ds2?.length && !this.ds2Columns().length) {
          this.ds2Columns.set(headers.ds2);
        }
        if (this.resultType() === 'missing_from_ds2' && headers?.ds1?.length && !this.missingColumns().length) {
          this.missingColumns.set(headers.ds1);
        } else if (this.resultType() === 'missing_from_ds1' && headers?.ds2?.length && !this.missingColumns().length) {
          this.missingColumns.set(headers.ds2);
        }
      },
      error: () => {
        // Fall back to extracting headers dynamically from row records
      }
    });
  }

  loadData(): void {
    const id = this.comparisonId();
    const type = this.resultType();

    if (!id) {
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    if (type === 'mismatches') {
      this.comparisonService.getMismatches(id, this.pageIndex(), this.pageSize()).subscribe({
        next: (result) => {
          this.mismatchRows.set(result.content || []);
          this.totalElements.set(result.totalElements || 0);
          this.extractMismatchColumns(result.content || []);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.errorMessage.set(err.message || 'Failed to load mismatch records.');
          this.isLoading.set(false);
        }
      });
    } else {
      const isMissingDs2 = type === 'missing_from_ds2';
      const direction = isMissingDs2 ? 'ds1' : 'ds2';
      const errorContext = isMissingDs2 ? 'Dataset 2' : 'Dataset 1';

      this.comparisonService.getMissing(id, this.pageIndex(), this.pageSize(), direction).subscribe({
        next: (result) => {
          this.missingRows.set(result.content || []);
          this.totalElements.set(result.totalElements || 0);
          this.extractMissingColumns(result.content || []);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.errorMessage.set(err.message || `Failed to load records missing from ${errorContext}.`);
          this.isLoading.set(false);
        }
      });
    }
  }

  private extractMismatchColumns(rows: MismatchDetail[]): void {
    if (rows.length === 0) return;

    const ds1ColSet = new Set<string>(this.ds1Columns());
    const ds2ColSet = new Set<string>(this.ds2Columns());

    for (const row of rows) {
      const data1 = row.dataDs1 || {};
      for (const k of Object.keys(data1)) {
        if (k !== '_row_id' && k !== '_diff_columns') {
          ds1ColSet.add(k);
        }
      }

      const data2 = row.dataDs2 || {};
      for (const k of Object.keys(data2)) {
        if (k !== '_row_id' && k !== '_diff_columns') {
          ds2ColSet.add(k);
        }
      }
    }

    this.ds1Columns.set(Array.from(ds1ColSet));
    this.ds2Columns.set(Array.from(ds2ColSet));
  }

  private extractMissingColumns(rows: MissingDetail[]): void {
    if (rows.length === 0) return;

    const colSet = new Set<string>(this.missingColumns());

    for (const row of rows) {
      const data = row.data || {};
      for (const k of Object.keys(data)) {
        if (k !== '_row_id' && k !== '_diff_columns') {
          colSet.add(k);
        }
      }
    }

    this.missingColumns.set(Array.from(colSet));
  }

  isDiffering(row: MismatchDetail, col: string): boolean {
    const diffs = row.differingColumns || [];
    return diffs.some(
      (c: string) => c.toLowerCase() === col.toLowerCase() || c === col
    );
  }

  getDs1Value(row: MismatchDetail, col: string): unknown {
    return row.dataDs1 ? row.dataDs1[col] : undefined;
  }

  getDs2Value(row: MismatchDetail, col: string): unknown {
    return row.dataDs2 ? row.dataDs2[col] : undefined;
  }

  getMissingValue(row: MissingDetail, col: string): unknown {
    return row.data ? row.data[col] : undefined;
  }

  formatValue(val: unknown): string {
    if (val === null || val === undefined) {
      return 'NULL';
    }
    if (typeof val === 'object') {
      return JSON.stringify(val);
    }
    return String(val);
  }

  isNullValue(val: unknown): boolean {
    return val === null || val === undefined;
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadData();
  }
}
