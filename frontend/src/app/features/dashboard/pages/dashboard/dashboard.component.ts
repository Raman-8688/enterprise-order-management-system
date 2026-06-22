import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { OrderService } from '../../../orders/services/order.service';
import { ProductService } from '../../../products/services/product.service';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { Order } from '../../../orders/models/order.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatIconModule, MatButtonModule, MatChipsModule,
    TopbarComponent, LoadingSpinnerComponent, BadgeComponent, EmptyStateComponent
  ],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  loading = true;
  today = new Date().toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  stats = { totalOrders: 0, confirmedOrders: 0, pendingOrders: 0, failedOrders: 0, totalProducts: 0, lowStockItems: 0 };
  recentOrders: Order[] = [];
  barData = [
    { month: 'Jan', pct: 55 }, { month: 'Feb', pct: 70 },
    { month: 'Mar', pct: 48 }, { month: 'Apr', pct: 82 },
    { month: 'May', pct: 65 }, { month: 'Jun', pct: 90 },
    { month: 'Jul', pct: 100 }
  ];
  statusDistribution = [
    { label: 'Confirmed',  color: '#16a34a', pct: 0 },
    { label: 'Processing', color: '#3b82f6', pct: 15 },
    { label: 'Pending',    color: '#f59e0b', pct: 0 },
    { label: 'Failed',     color: '#ef4444', pct: 0 }
  ];

  constructor(
    private orderService: OrderService,
    private productService: ProductService,
    private inventoryService: InventoryService
  ) {}

  ngOnInit() {
    forkJoin({
      allOrders: this.orderService.getAll(0, 50).pipe(catchError(() => of(null))),
      confirmed: this.orderService.getByStatus('CONFIRMED', 0, 5).pipe(catchError(() => of(null))),
      pending:   this.orderService.getByStatus('PENDING',   0, 1).pipe(catchError(() => of(null))),
      failed:    this.orderService.getByStatus('FAILED',    0, 1).pipe(catchError(() => of(null))),
      products:  this.productService.getAll(0, 1).pipe(catchError(() => of(null))),
      inventory: this.inventoryService.getAll().pipe(catchError(() => of([])))
    }).subscribe(({ allOrders, confirmed, pending, failed, products, inventory }) => {
      this.stats.totalOrders     = allOrders?.totalElements  ?? 0;
      this.stats.confirmedOrders = confirmed?.totalElements  ?? 0;
      this.stats.pendingOrders   = pending?.totalElements    ?? 0;
      this.stats.failedOrders    = failed?.totalElements     ?? 0;
      this.stats.totalProducts   = products?.totalElements   ?? 0;
      const inv = (inventory as any[]) ?? [];
      this.stats.lowStockItems   = inv.filter((i: any) => i.availableQuantity <= i.reorderPoint).length;
      this.recentOrders = (confirmed?.content ?? []) as any[];
      const total = this.stats.totalOrders || 1;
      this.statusDistribution[0].pct = Math.round((this.stats.confirmedOrders / total) * 100);
      this.statusDistribution[2].pct = Math.round((this.stats.pendingOrders   / total) * 100);
      this.statusDistribution[3].pct = Math.round((this.stats.failedOrders    / total) * 100);
      this.loading = false;
    });
  }

  getStatusBadgeType(status: string) { return BadgeComponent.forOrderStatus(status); }
  formatCurrency(a: number) {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(a);
  }
}
