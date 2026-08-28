import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ComparisonService } from '../../services/comparison.service';
import { ComparisonStatus, ComparisonSummary, DataSourceType } from '../../models/comparison.model';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatChipsModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatCardModule,
    MatProgressBarModule
  ],
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss'
})
export class HistoryComponent implements OnInit, OnDestroy {
  private readonly comparisonService = inject(ComparisonService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  displayedColumns: string[] = [
    'id',
    'createdAt',
    'status',
    'ds1Source',
    'ds2Source',
    'records',
    'actions'
  ];

  comparisons = signal<ComparisonSummary[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);
  isDeleting = signal<Record<string, boolean>>({});

  private refreshSub?: Subscription;

  ngOnInit(): void {
    this.loadComparisons();
    this.setupAutoRefresh();
  }

  setupAutoRefresh(): void {
    this.refreshSub = interval(30000).subscribe(() => {
      this.loadComparisons(true);
    });
  }

  loadComparisons(isBackground = false): void {
    if (!isBackground) {
      this.isLoading.set(true);
    }
    this.errorMessage.set(null);

    this.comparisonService.listComparisons().subscribe({
      next: (data) => {
        // Sort descending by createdAt if available
        const sorted = [...(data || [])].sort((a, b) => {
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        });
        this.comparisons.set(sorted);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.message || 'Failed to load comparison history');
      }
    });
  }

  deleteComparison(id: string): void {
    if (this.isDeleting()[id]) return;

    this.isDeleting.update((map) => ({ ...map, [id]: true }));

    this.comparisonService.deleteComparison(id).subscribe({
      next: () => {
        this.isDeleting.update((map) => {
          const updated = { ...map };
          delete updated[id];
          return updated;
        });
        this.comparisons.update((list) => list.filter((item) => item.id !== id));
        this.snackBar.open('Comparison deleted successfully', 'Dismiss', {
          duration: 3000
        });
      },
      error: (err) => {
        this.isDeleting.update((map) => {
          const updated = { ...map };
          delete updated[id];
          return updated;
        });
        this.snackBar.open(
          `Failed to delete comparison: ${err.message || 'Server error'}`,
          'Dismiss',
          { duration: 4000 }
        );
      }
    });
  }

  navigateToResults(id: string): void {
    this.router.navigate(['/results', id]);
  }

  getStatusClass(status: ComparisonStatus): string {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return 'status-completed';
      case 'COMPARING':
      case 'CONVERTING':
      case 'UPLOADING':
        return 'status-comparing';
      case 'FAILED':
        return 'status-failed';
      case 'PENDING':
      case 'UPLOADED':
      default:
        return 'status-pending';
    }
  }

  getSourceLabel(type?: DataSourceType, fileName?: string): string {
    if (type === 'SQL_QUERY') {
      return 'SQL Query';
    }
    if (fileName) {
      return fileName;
    }
    if (type === 'FILE_UPLOAD') {
      return 'Uploaded File';
    }
    return '-';
  }

  formatRecords(ds1Count?: number, ds2Count?: number): string {
    const ds1Str = ds1Count !== undefined && ds1Count !== null ? ds1Count.toLocaleString() : '-';
    const ds2Str = ds2Count !== undefined && ds2Count !== null ? ds2Count.toLocaleString() : '-';
    return `${ds1Str} / ${ds2Str}`;
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }
}
