import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-column-selector',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule
  ],
  template: `
    <div class="column-selector-container">
      <div class="selection-controls">
        <mat-form-field appearance="outline" class="select-dropdown">
          <mat-label>Select Key Column(s)</mat-label>
          <mat-select
            multiple
            [(ngModel)]="selectedKeysModel"
            (ngModelChange)="onSelectChange($event)"
            [disabled]="disabled()"
            placeholder="Choose columns to match on"
          >
            @for (col of allOptions(); track col) {
              <mat-option [value]="col">{{ col }}</mat-option>
            }
          </mat-select>
          <mat-hint>Columns used to join and compare dataset records</mat-hint>
        </mat-form-field>

        <div class="manual-entry-row">
          <mat-form-field appearance="outline" class="manual-input">
            <mat-label>Or Type Column Name</mat-label>
            <input
              matInput
              [(ngModel)]="manualColumnModel"
              (ngModelChange)="manualColumnInput.set($event)"
              (keydown.enter)="$event.preventDefault(); addManualColumn()"
              [disabled]="disabled()"
              placeholder="e.g. custom_id"
            />
          </mat-form-field>
          <button
            mat-stroked-button
            type="button"
            class="add-manual-btn"
            (click)="addManualColumn()"
            [disabled]="disabled() || !manualColumnInput().trim()"
          >
            <mat-icon>add</mat-icon>
            Add
          </button>
        </div>
      </div>

      @if (selectedKeys().length > 0) {
        <div class="selected-chips-wrapper">
          <span class="chips-label">Active Key Columns:</span>
          <mat-chip-set class="key-chips">
            @for (key of selectedKeys(); track key) {
              <mat-chip-row (removed)="removeKey(key)">
                {{ key }}
                <button matChipRemove [disabled]="disabled()" [attr.aria-label]="'Remove ' + key">
                  <mat-icon>cancel</mat-icon>
                </button>
              </mat-chip-row>
            }
          </mat-chip-set>
        </div>
      }
    </div>
  `,
  styles: [`
    .column-selector-container {
      display: flex;
      flex-direction: column;
      gap: 12px;
      width: 100%;
    }

    .selection-controls {
      display: flex;
      gap: 16px;
      align-items: flex-start;
      flex-wrap: wrap;
    }

    .select-dropdown {
      flex: 1;
      min-width: 260px;
    }

    .manual-entry-row {
      display: flex;
      gap: 8px;
      align-items: flex-start;
    }

    .manual-input {
      width: 200px;
    }

    .add-manual-btn {
      height: 56px;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .selected-chips-wrapper {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
      padding: 4px 0;
    }

    .chips-label {
      font-size: 0.875rem;
      font-weight: 500;
      color: #475569;
    }

    .key-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }
  `]
})
export class ColumnSelectorComponent {
  availableColumns = input<string[]>([]);
  disabled = input<boolean>(false);

  selectedKeysChange = output<string[]>();

  selectedKeys = signal<string[]>([]);
  selectedKeysModel: string[] = [];

  manualColumnInput = signal<string>('');
  manualColumnModel = '';

  manuallyAddedColumns = signal<string[]>([]);

  allOptions(): string[] {
    const combined = new Set([...this.availableColumns(), ...this.manuallyAddedColumns()]);
    return Array.from(combined);
  }

  onSelectChange(keys: string[]): void {
    this.selectedKeys.set(keys || []);
    this.selectedKeysModel = keys || [];
    this.selectedKeysChange.emit(this.selectedKeys());
  }

  addManualColumn(): void {
    const col = this.manualColumnInput().trim();
    if (!col) return;

    if (!this.manuallyAddedColumns().includes(col)) {
      this.manuallyAddedColumns.update(list => [...list, col]);
    }

    if (!this.selectedKeys().includes(col)) {
      const updated = [...this.selectedKeys(), col];
      this.selectedKeys.set(updated);
      this.selectedKeysModel = updated;
      this.selectedKeysChange.emit(updated);
    }

    this.manualColumnInput.set('');
    this.manualColumnModel = '';
  }

  removeKey(keyToRemove: string): void {
    const updated = this.selectedKeys().filter(k => k !== keyToRemove);
    this.selectedKeys.set(updated);
    this.selectedKeysModel = updated;
    this.selectedKeysChange.emit(updated);
  }
}
