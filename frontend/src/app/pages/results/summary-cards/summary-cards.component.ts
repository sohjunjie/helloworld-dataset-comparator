import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ComparisonSummary } from '../../../models/comparison.model';

@Component({
  selector: 'app-summary-cards',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  templateUrl: './summary-cards.component.html',
  styleUrl: './summary-cards.component.scss'
})
export class SummaryCardsComponent {
  summary = input.required<ComparisonSummary>();

  get ds1Total(): number {
    return this.summary()?.ds1RecordCount ?? 0;
  }

  get ds2Total(): number {
    return this.summary()?.ds2RecordCount ?? 0;
  }

  get ds1Matching(): number {
    return this.summary()?.ds1FullyMatching ?? 0;
  }

  get ds2Matching(): number {
    return this.summary()?.ds2FullyMatching ?? 0;
  }

  get ds1NotMatching(): number {
    return this.summary()?.ds1NotMatching ?? 0;
  }

  get ds2NotMatching(): number {
    return this.summary()?.ds2NotMatching ?? 0;
  }

  get missingInDs2(): number {
    return this.summary()?.ds1MissingInDs2 ?? 0;
  }

  get missingInDs1(): number {
    return this.summary()?.ds2MissingInDs1 ?? 0;
  }
}
