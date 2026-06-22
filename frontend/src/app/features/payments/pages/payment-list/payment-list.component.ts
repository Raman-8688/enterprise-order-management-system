import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { PaymentService } from '../../services/payment.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Payment } from '../../models/payment.model';

@Component({
  selector: 'app-payment-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatTableModule, MatChipsModule, MatIconModule, MatButtonModule,
    MatInputModule, MatFormFieldModule, MatTooltipModule,
    MatProgressSpinnerModule, MatDividerModule,
    TopbarComponent, PageHeaderComponent, BadgeComponent,
    EmptyStateComponent, LoadingSpinnerComponent
  ],
  templateUrl: './payment-list.component.html'
})
export class PaymentListComponent implements OnInit {
  payments: Payment[] = [];
  loading = true;
  searchOrderId = '';
  searchedPayment: Payment | null = null;
  searching = false;
  searchError = '';
  stats = { total: 0, completed: 0, failed: 0, totalAmount: 0 };
  displayedColumns = ['paymentId', 'orderId', 'amount', 'status', 'method', 'createdAt'];

  constructor(
    private paymentService: PaymentService,
    private notif: NotificationService
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.paymentService.getAll(0, 50).subscribe({
      next: (data) => {
        this.payments = Array.isArray(data) ? data : [];
        this.computeStats();
        this.loading = false;
      },
      error: () => { this.payments = []; this.loading = false; }
    });
  }

  computeStats() {
    this.stats.total       = this.payments.length;
    this.stats.completed   = this.payments.filter(p => p.status === 'COMPLETED').length;
    this.stats.failed      = this.payments.filter(p => p.status === 'FAILED').length;
    this.stats.totalAmount = this.payments
      .filter(p => p.status === 'COMPLETED')
      .reduce((s, p) => s + (p.amount || 0), 0);
  }

  searchByOrder() {
    if (!this.searchOrderId.trim()) { this.notif.error('Enter an Order ID'); return; }
    this.searching = true;
    this.searchError = '';
    this.searchedPayment = null;
    this.paymentService.getByOrderId(this.searchOrderId.trim()).subscribe({
      next: (p) => { this.searchedPayment = p; this.searching = false; },
      error: () => { this.searchError = 'No payment found for this order ID.'; this.searching = false; }
    });
  }

  clearSearch() { this.searchOrderId = ''; this.searchedPayment = null; this.searchError = ''; }

  getBadgeType(status: string): any {
    const map: Record<string, string> = {
      COMPLETED: 'success', PENDING: 'warning', FAILED: 'danger', REFUNDED: 'info'
    };
    return map[status] ?? 'muted';
  }
}
