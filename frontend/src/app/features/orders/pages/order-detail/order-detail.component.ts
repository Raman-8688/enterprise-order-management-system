import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { OrderService } from '../../services/order.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Order, OrderStatus } from '../../models/order.model';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatIconModule, MatButtonModule, MatDividerModule, MatChipsModule,
    TopbarComponent, BadgeComponent, LoadingSpinnerComponent, PageHeaderComponent, ConfirmDialogComponent
  ],
  templateUrl: './order-detail.component.html'
})
export class OrderDetailComponent implements OnInit {
  order: Order | null = null;
  loading = true;
  showCancel = false;

  constructor(private route: ActivatedRoute, private orderService: OrderService, private notif: NotificationService) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.orderService.getById(id).subscribe({
      next: (o) => { this.order = o; this.loading = false; },
      error: () => { this.notif.error('Order not found'); this.loading = false; }
    });
  }

  doCancel() {
    if (!this.order) return;
    this.showCancel = false;
    this.orderService.cancel(this.order.id).subscribe({
      next: () => { this.notif.success('Order cancelled'); this.order!.status = 'CANCELLED'; },
      error: () => this.notif.error('Cancel failed')
    });
  }

  getBadgeType(s: string) { return BadgeComponent.forOrderStatus(s); }

  get timelineSteps() {
    const all: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];
    const idx = all.indexOf(this.order?.status as OrderStatus);
    return all.map((s, i) => ({ label: s, done: i <= idx, active: i === idx }));
  }

  get canCancel() { return ['PENDING', 'CONFIRMED'].includes(this.order?.status ?? ''); }
}
