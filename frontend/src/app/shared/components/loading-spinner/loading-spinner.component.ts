import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    @if (overlay) {
      <div class="spinner-overlay">
        <mat-spinner diameter="40" color="primary"></mat-spinner>
      </div>
    } @else {
      <div style="display:flex;align-items:center;justify-content:center;padding:48px">
        <mat-spinner diameter="36" color="primary"></mat-spinner>
      </div>
    }
  `
})
export class LoadingSpinnerComponent {
  @Input() overlay = false;
}
