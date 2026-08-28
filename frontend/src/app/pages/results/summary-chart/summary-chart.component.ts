import { Component, computed, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { ComparisonSummary } from '../../../models/comparison.model';

@Component({
  selector: 'app-summary-chart',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonToggleModule,
    MatIconModule,
    BaseChartDirective
  ],
  templateUrl: './summary-chart.component.html',
  styleUrl: './summary-chart.component.scss'
})
export class SummaryChartComponent {
  summary = input.required<ComparisonSummary>();
  chartType = signal<'bar' | 'doughnut'>('bar');

  private readonly chartColors = [
    '#2e7d32', // Fully Matching (Green)
    '#ed6c02', // DS1 Mismatched (Amber/Orange)
    '#f59e0b', // DS2 Mismatched (Light Orange)
    '#d32f2f', // Missing in DS2 (Red)
    '#7b1fa2'  // Missing in DS1 (Purple)
  ];

  barChartData = computed<ChartData<'bar'>>(() => {
    const s = this.summary();
    const matchCount = Math.max(s?.ds1FullyMatching ?? 0, s?.ds2FullyMatching ?? 0);
    return {
      labels: [
        'Fully Matching',
        'DS1 Mismatched',
        'DS2 Mismatched',
        'Missing from DS2',
        'Missing from DS1'
      ],
      datasets: [
        {
          data: [
            matchCount,
            s?.ds1NotMatching ?? 0,
            s?.ds2NotMatching ?? 0,
            s?.ds1MissingInDs2 ?? 0,
            s?.ds2MissingInDs1 ?? 0
          ],
          label: 'Records',
          backgroundColor: this.chartColors,
          borderColor: this.chartColors,
          borderWidth: 1,
          borderRadius: 4
        }
      ]
    };
  });

  doughnutChartData = computed<ChartData<'doughnut'>>(() => {
    const s = this.summary();
    const matchCount = Math.max(s?.ds1FullyMatching ?? 0, s?.ds2FullyMatching ?? 0);
    return {
      labels: [
        'Fully Matching',
        'DS1 Mismatched',
        'DS2 Mismatched',
        'Missing from DS2',
        'Missing from DS1'
      ],
      datasets: [
        {
          data: [
            matchCount,
            s?.ds1NotMatching ?? 0,
            s?.ds2NotMatching ?? 0,
            s?.ds1MissingInDs2 ?? 0,
            s?.ds2MissingInDs1 ?? 0
          ],
          backgroundColor: this.chartColors,
          hoverOffset: 6
        }
      ]
    };
  });

  activeChartData = computed<ChartData<any>>(() => {
    return this.chartType() === 'bar' ? this.barChartData() : this.doughnutChartData();
  });

  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          boxWidth: 14,
          padding: 16,
          font: {
            family: 'Roboto, "Helvetica Neue", sans-serif',
            size: 12
          }
        }
      },
      tooltip: {
        padding: 10,
        cornerRadius: 6,
        callbacks: {
          label: (context) => {
            const val = context.raw as number;
            return ` ${context.label || ''}: ${val.toLocaleString()} records`;
          }
        }
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        }
      },
      y: {
        beginAtZero: true,
        ticks: {
          precision: 0
        }
      }
    }
  };

  doughnutOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          boxWidth: 14,
          padding: 16,
          font: {
            family: 'Roboto, "Helvetica Neue", sans-serif',
            size: 12
          }
        }
      },
      tooltip: {
        padding: 10,
        cornerRadius: 6,
        callbacks: {
          label: (context) => {
            const val = context.raw as number;
            return ` ${context.label || ''}: ${val.toLocaleString()} records`;
          }
        }
      }
    }
  };

  setChartType(type: 'bar' | 'doughnut'): void {
    this.chartType.set(type);
  }
}
