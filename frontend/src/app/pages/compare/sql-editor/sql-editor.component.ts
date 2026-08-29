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
  templateUrl: './sql-editor.component.html',
  styleUrl: './sql-editor.component.scss'
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
