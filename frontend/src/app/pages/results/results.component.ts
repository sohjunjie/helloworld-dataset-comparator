import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';

import { ComparisonService } from '../../services/comparison.service';
import { ProgressService } from '../../services/progress.service';
import { ComparisonSummary } from '../../models/comparison.model';
import { SummaryCardsComponent } from './summary-cards/summary-cards.component';
import { SummaryChartComponent } from './summary-chart/summary-chart.component';
import { DetailTableComponent } from './detail-table/detail-table.component';

@Component({
  selector: 'app-results',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    SummaryCardsComponent,
    SummaryChartComponent,
    DetailTableComponent
  ],
  templateUrl: './results.component.html',
  styleUrl: './results.component.scss'
})
export class ResultsComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly comparisonService = inject(ComparisonService);
  private readonly progressService = inject(ProgressService);

  comparisonId = signal<string>('');
  isLoading = signal<boolean>(true);
  isDownloading = signal<boolean>(false);
  currentStage = signal<string>('Initializing comparison...');
  progressPercent = signal<number>(0);
  summary = signal<ComparisonSummary | null>(null);
  errorMessage = signal<string | null>(null);

  private progressSub?: Subscription;
  private summarySub?: Subscription;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('comparisonId') || '';
    this.comparisonId.set(id);

    if (!id) {
      this.errorMessage.set('No comparison ID specified in route.');
      this.isLoading.set(false);
      return;
    }

    this.fetchInitialSummary(id);
    this.subscribeToProgress(id);
  }

  private fetchInitialSummary(id: string): void {
    this.summarySub = this.comparisonService.getComparison(id).subscribe({
      next: (summary) => {
        if (summary.status === 'COMPLETED') {
          this.summary.set(summary);
          this.isLoading.set(false);
        } else if (summary.status === 'FAILED') {
          this.errorMessage.set(summary.errorMessage || 'Comparison failed');
          this.isLoading.set(false);
        } else {
          this.isLoading.set(true);
          this.currentStage.set(this.formatStageName(summary.status));
        }
      },
      error: (err) => {
        // If SSE hasn't failed yet, don't necessarily abort, but record error if persistent
        if (this.isLoading()) {
          // SSE subscription might still be running
        } else {
          this.errorMessage.set(err.message || 'Failed to load comparison metadata');
          this.isLoading.set(false);
        }
      }
    });
  }

  private subscribeToProgress(id: string): void {
    this.progressSub = this.progressService.subscribe(id).subscribe({
      next: (update) => {
        if (update.stage) {
          this.currentStage.set(this.formatStageName(update.stage));
        }
        if (typeof update.percent === 'number') {
          this.progressPercent.set(update.percent);
        }

        if (update.stage === 'COMPLETED') {
          this.loadFinalSummary(id);
        } else if (update.stage === 'FAILED') {
          this.errorMessage.set(update.message || 'Comparison failed');
          this.isLoading.set(false);
        }
      },
      error: (err) => {
        // Check if summary was already completed
        if (this.summary()?.status === 'COMPLETED') {
          return;
        }
        this.errorMessage.set(err.message || 'Connection lost or comparison failed');
        this.isLoading.set(false);
      }
    });
  }

  private loadFinalSummary(id: string): void {
    this.comparisonService.getComparison(id).subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.message || 'Failed to fetch final summary data');
        this.isLoading.set(false);
      }
    });
  }

  private formatStageName(stage: string): string {
    switch (stage?.toUpperCase()) {
      case 'PENDING':
        return 'Queued...';
      case 'UPLOADING':
        return 'Uploading datasets...';
      case 'CONVERTING':
        return 'Converting datasets to Parquet...';
      case 'COMPARING':
        return 'Comparing dataset records...';
      case 'COMPLETED':
        return 'Completed';
      case 'FAILED':
        return 'Failed';
      default:
        return stage || 'Processing...';
    }
  }

  downloadReport(): void {
    const id = this.comparisonId();
    if (!id || this.isDownloading()) return;

    this.isDownloading.set(true);
    this.comparisonService.downloadReport(id).subscribe({
      next: (blob) => {
        this.isDownloading.set(false);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `comparison-${id}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.isDownloading.set(false);
        this.errorMessage.set(err.message || 'Failed to download comparison report.');
      }
    });
  }

  ngOnDestroy(): void {
    this.progressSub?.unsubscribe();
    this.summarySub?.unsubscribe();
  }
}
