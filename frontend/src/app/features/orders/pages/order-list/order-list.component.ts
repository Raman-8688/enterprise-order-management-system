import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { OrderService } from '../../services/order.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Order, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatIconModule, MatButtonModule, MatTooltipModule, MatChipsModule,
    TopbarComponent, BadgeComponent, LoadingSpinnerComponent,
    EmptyStateComponent, PageHeaderComponent, ConfirmDialogComponent
  ],
  templateUrl: './order-list.component.html'
})
export class OrderListComponent implements OnInit {
  orders: Order[] = [];
  loading = true;
  total = 0; page = 0; size = 10;
  activeTab = 'ALL';
  selectedOrder: Order | null = null;
  showCancel = false;

  tabs = [
    { label: 'All', value: 'ALL' }, { label: 'Confirmed', value: 'CONFIRMED' },
    { label: 'Processing', value: 'PROCESSING' }, { label: 'Pending', value: 'PENDING' },
    { label: 'Failed', value: 'FAILED' }, { label: 'Cancelled', value: 'CANCELLED' }
  ];

  constructor(private orderService: OrderService, private notif: NotificationService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    const obs = this.activeTab === 'ALL'
      ? this.orderService.getAll(this.page, this.size)
      : this.orderService.getByStatus(this.activeTab, this.page, this.size);
    obs.subscribe({
      next: (p: any) => { this.orders = p.content ?? []; this.total = p.totalElements ?? 0; this.loading = false; },
      error: () => { this.notif.error('Failed to load orders'); this.loading = false; }
    });
  }

  switchTab(tab: string) { this.activeTab = tab; this.page = 0; this.load(); }
  goToPage(p: number)    { this.page = p; this.load(); }
  selectOrder(order: Order) { this.selectedOrder = order; }
  confirmCancel(order: Order) { this.selectedOrder = order; this.showCancel = true; }

  doCancel() {
    if (!this.selectedOrder) return;
    this.showCancel = false;
    this.orderService.cancel(this.selectedOrder.id).subscribe({
      next: () => { this.notif.success('Order cancelled'); this.load(); },
      error: () => this.notif.error('Could not cancel order')
    });
  }

  get totalPages() { return Math.ceil(this.total / this.size); }
  get pages()      { return Array.from({ length: Math.min(this.totalPages, 5) }, (_, i) => i); }
  getBadgeType(status: string) { return BadgeComponent.forOrderStatus(status); }

  timelineSteps(status: OrderStatus) {
    const all: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];
    const idx = all.indexOf(status);
    return all.map((s, i) => ({ label: s, done: i <= idx, active: i === idx }));
  }

  canCancel(status: string) { return ['PENDING', 'CONFIRMED'].includes(status); }
}
