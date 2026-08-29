import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { ErrorStateMatcher } from '@angular/material/core';
import { InstantErrorStateMatcher } from '../../../utils/instant-error-state-matcher';

@Component({
  selector: 'app-column-selector',
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
    MatChipsModule,
    MatIconModule
  ],
  templateUrl: './column-selector.component.html',
  styleUrl: './column-selector.component.scss'
})
export class ColumnSelectorComponent {
  readonly errorMatcher = new InstantErrorStateMatcher();

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

  setSelectedKeys(keys: string[]): void {
    this.selectedKeysModel = keys || [];
    this.selectedKeys.set(keys || []);
    this.selectedKeysChange.emit(this.selectedKeys());
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

  isValid(): boolean {
    return this.selectedKeys().length > 0;
  }
}

