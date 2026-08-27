import {
  Component,
  ElementRef,
  ViewChild,
  AfterViewInit,
  OnDestroy,
  input,
  output,
  effect,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { basicSetup } from 'codemirror';
import { sql, PostgreSQL } from '@codemirror/lang-sql';
import { EditorState, Compartment } from '@codemirror/state';
import { EditorView, placeholder as cmPlaceholder } from '@codemirror/view';

@Component({
  selector: 'app-sql-editor',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="sql-editor-wrapper">
      <div class="sql-editor-header">
        <span class="editor-label">SQL Query (PostgreSQL SELECT)</span>
      </div>
      <div
        #editorContainer
        class="editor-container"
        [class.is-disabled]="disabled()"
      ></div>
    </div>
  `,
  styles: [`
    .sql-editor-wrapper {
      display: flex;
      flex-direction: column;
      gap: 6px;
      width: 100%;
    }

    .sql-editor-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .editor-label {
      font-size: 0.8125rem;
      font-weight: 600;
      color: #475569;
      text-transform: uppercase;
      letter-spacing: 0.025em;
    }

    .editor-container {
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      overflow: hidden;
      background-color: #ffffff;
      transition: border-color 0.2s, box-shadow 0.2s;

      &:focus-within {
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
      }

      &.is-disabled {
        opacity: 0.65;
        pointer-events: none;
        background-color: #f8fafc;
      }
    }

    ::ng-deep .cm-editor {
      min-height: 150px;
      max-height: 320px;
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
      font-size: 13.5px;
    }

    ::ng-deep .cm-scroller {
      min-height: 150px;
      overflow: auto;
      line-height: 1.5;
    }

    ::ng-deep .cm-gutters {
      background-color: #f8fafc;
      color: #94a3b8;
      border-right: 1px solid #e2e8f0;
    }

    ::ng-deep .cm-activeLineGutter {
      background-color: #e2e8f0;
      color: #1e293b;
    }

    ::ng-deep .cm-activeLine {
      background-color: #f1f5f9;
    }
  `]
})
export class SqlEditorComponent implements AfterViewInit, OnDestroy {
  @ViewChild('editorContainer') private editorContainerRef!: ElementRef<HTMLDivElement>;

  query = input<string>('');
  disabled = input<boolean>(false);
  placeholderText = input<string>('SELECT * FROM table_name');

  queryChange = output<string>();

  private editorView: EditorView | null = null;
  private readonly disabledCompartment = new Compartment();
  private isInternalChange = false;

  constructor() {
    effect(() => {
      const externalQuery = this.query();
      if (this.editorView && !this.isInternalChange) {
        const currentDoc = this.editorView.state.doc.toString();
        if (currentDoc !== externalQuery) {
          this.editorView.dispatch({
            changes: { from: 0, to: currentDoc.length, insert: externalQuery || '' }
          });
        }
      }
    });

    effect(() => {
      const isDisabled = this.disabled();
      if (this.editorView) {
        this.editorView.dispatch({
          effects: this.disabledCompartment.reconfigure(EditorView.editable.of(!isDisabled))
        });
      }
    });
  }

  ngAfterViewInit(): void {
    this.initEditor();
  }

  ngOnDestroy(): void {
    if (this.editorView) {
      this.editorView.destroy();
      this.editorView = null;
    }
  }

  getEditorView(): EditorView | null {
    return this.editorView;
  }

  getQuery(): string {
    return this.editorView ? this.editorView.state.doc.toString() : '';
  }

  setQuery(sqlText: string): void {
    if (this.editorView) {
      const currentDoc = this.editorView.state.doc.toString();
      if (currentDoc !== sqlText) {
        this.editorView.dispatch({
          changes: { from: 0, to: currentDoc.length, insert: sqlText }
        });
      }
    }
  }

  private initEditor(): void {
    if (!this.editorContainerRef?.nativeElement) return;

    const initialText = this.query() || '';

    const state = EditorState.create({
      doc: initialText,
      extensions: [
        basicSetup,
        sql({ dialect: PostgreSQL }),
        cmPlaceholder(this.placeholderText()),
        this.disabledCompartment.of(EditorView.editable.of(!this.disabled())),
        EditorView.updateListener.of((update) => {
          if (update.docChanged) {
            const newDoc = update.state.doc.toString();
            this.isInternalChange = true;
            this.queryChange.emit(newDoc);
            this.isInternalChange = false;
          }
        })
      ]
    });

    this.editorView = new EditorView({
      state,
      parent: this.editorContainerRef.nativeElement
    });
  }
}
