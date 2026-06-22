import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService, Toast } from '../../../core/services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="oms-toast-container">
      @for (toast of toasts; track toast.id) {
        <div class="oms-toast" [ngClass]="toast.type">
          <mat-icon style="font-size:18px;width:18px;height:18px">{{ iconMap[toast.type] }}</mat-icon>
          <span style="flex:1">{{ toast.message }}</span>
          <mat-icon style="font-size:16px;width:16px;height:16px;cursor:pointer;opacity:0.8"
                    (click)="notif.dismiss(toast.id)">close</mat-icon>
        </div>
      }
    </div>
  `
})
export class ToastComponent implements OnInit {
  toasts: Toast[] = [];
  iconMap: Record<string, string> = {
    success: 'check_circle', error: 'error', warning: 'warning', info: 'info'
  };
  constructor(public notif: NotificationService) {}
  ngOnInit() { this.notif.toasts$.subscribe(t => this.toasts = t); }
}
