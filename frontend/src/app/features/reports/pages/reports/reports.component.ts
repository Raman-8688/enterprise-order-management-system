import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { OrderService } from '../../../orders/services/order.service';
import { ProductService } from '../../../products/services/product.service';
import { InventoryService } from '../../../inventory/services/inventory.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatIconModule, MatButtonModule, MatTabsModule,
    MatProgressBarModule, MatChipsModule, MatTooltipModule, MatDividerModule,
    TopbarComponent, PageHeaderComponent, LoadingSpinnerComponent, BadgeComponent
  ],
  templateUrl: './reports.component.html'
})
export class ReportsComponent implements OnInit {
  loading = true;
  orderStats   = { total: 0, confirmed: 0, failed: 0, pending: 0, cancelled: 0 };
  productStats = { total: 0 };
  inventoryStats = { inStock: 0, lowStock: 0, outOfStock: 0 };
  statusRows: { label: string; count: number; pct: number; color: string }[] = [];
  months        = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'];
  confirmedBars = [12, 19, 14, 28, 22, 35, 40];
  failedBars    = [3,  2,  5,  1,  4,  2,  3];
  maxBar = 40;

  constructor(
    private orderService: OrderService,
    private productService: ProductService,
    private inventoryService: InventoryService
  ) {}

  ngOnInit() {
    forkJoin({
      confirmed:  this.orderService.getByStatus('CONFIRMED',  0, 1).pipe(catchError(() => of(null))),
      failed:     this.orderService.getByStatus('FAILED',     0, 1).pipe(catchError(() => of(null))),
      pending:    this.orderService.getByStatus('PENDING',    0, 1).pipe(catchError(() => of(null))),
      cancelled:  this.orderService.getByStatus('CANCELLED',  0, 1).pipe(catchError(() => of(null))),
      products:   this.productService.getAll(0, 1).pipe(catchError(() => of(null))),
      inventory:  this.inventoryService.getAll().pipe(catchError(() => of([])))
    }).subscribe(({ confirmed, failed, pending, cancelled, products, inventory }) => {
      this.orderStats.confirmed  = confirmed?.totalElements  ?? 0;
      this.orderStats.failed     = failed?.totalElements     ?? 0;
      this.orderStats.pending    = pending?.totalElements    ?? 0;
      this.orderStats.cancelled  = cancelled?.totalElements  ?? 0;
      this.orderStats.total      = this.orderStats.confirmed + this.orderStats.failed +
                                   this.orderStats.pending   + this.orderStats.cancelled;
      this.productStats.total    = products?.totalElements ?? 0;
      const inv = (inventory as any[]) ?? [];
      this.inventoryStats.outOfStock = inv.filter((i: any) => i.availableQuantity === 0).length;
      this.inventoryStats.lowStock   = inv.filter((i: any) => i.availableQuantity > 0 && i.availableQuantity <= i.reorderPoint).length;
      this.inventoryStats.inStock    = inv.length - this.inventoryStats.outOfStock - this.inventoryStats.lowStock;
      const total = this.orderStats.total || 1;
      this.statusRows = [
        { label: 'Confirmed',  count: this.orderStats.confirmed,  pct: Math.round(this.orderStats.confirmed  / total * 100), color: '#16a34a' },
        { label: 'Failed',     count: this.orderStats.failed,     pct: Math.round(this.orderStats.failed     / total * 100), color: '#dc2626' },
        { label: 'Pending',    count: this.orderStats.pending,    pct: Math.round(this.orderStats.pending    / total * 100), color: '#d97706' },
        { label: 'Cancelled',  count: this.orderStats.cancelled,  pct: Math.round(this.orderStats.cancelled  / total * 100), color: '#64748b' }
      ];
      this.maxBar = Math.max(...this.confirmedBars, ...this.failedBars, 1);
      this.loading = false;
    });
  }

  barPct(val: number): number { return Math.round((val / this.maxBar) * 100); }

  get successRate(): number {
    return this.orderStats.total > 0
      ? Math.round((this.orderStats.confirmed / this.orderStats.total) * 100)
      : 0;
  }
}
