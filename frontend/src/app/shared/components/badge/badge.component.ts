import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type BadgeType = 'success' | 'warning' | 'danger' | 'info' | 'navy' | 'muted';

@Component({
  selector: 'app-badge',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="oms-badge" [ngClass]="'badge-' + type">{{ label }}</span>`
})
export class BadgeComponent {
  @Input() label = '';
  @Input() type: BadgeType | string = 'muted';

  static forOrderStatus(status: string): BadgeType {
    const map: Record<string, BadgeType> = {
      CONFIRMED: 'success', DELIVERED: 'success',
      PROCESSING: 'info',   SHIPPED: 'info',
      PENDING: 'warning',   FAILED: 'danger',
      CANCELLED: 'danger'
    };
    return map[status] ?? 'muted';
  }

  static forStockStatus(status: string): BadgeType {
    const map: Record<string, BadgeType> = {
      'in-stock': 'success', 'low-stock': 'warning', 'out-of-stock': 'danger'
    };
    return map[status] ?? 'muted';
  }
}
