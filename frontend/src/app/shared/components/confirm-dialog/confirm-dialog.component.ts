import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatDividerModule],
  template: `
    @if (visible) {
      <div class="oms-modal-backdrop" (click)="onCancel()">
        <div class="oms-modal" style="max-width:420px" (click)="$event.stopPropagation()">
          <div class="oms-modal-header">
            <h3 style="display:flex;align-items:center;gap:8px;font-size:16px;font-weight:600">
              <mat-icon style="color:var(--oms-danger);font-size:20px;width:20px;height:20px">warning</mat-icon>
              {{ title }}
            </h3>
            <button mat-icon-button (click)="onCancel()">
              <mat-icon>close</mat-icon>
            </button>
          </div>
          <div class="oms-modal-body">
            <p style="font-size:14px;color:var(--oms-text-secondary);line-height:1.6">{{ message }}</p>
          </div>
          <mat-divider></mat-divider>
          <div class="oms-modal-footer">
            <button mat-stroked-button (click)="onCancel()">Cancel</button>
            <button mat-raised-button color="warn" (click)="onConfirm()">
              {{ confirmLabel }}
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class ConfirmDialogComponent {
  @Input() visible = false;
  @Input() title = 'Confirm Action';
  @Input() message = 'Are you sure you want to proceed?';
  @Input() confirmLabel = 'Confirm';
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();
  onConfirm() { this.confirmed.emit(); }
  onCancel()  { this.cancelled.emit(); }
}
