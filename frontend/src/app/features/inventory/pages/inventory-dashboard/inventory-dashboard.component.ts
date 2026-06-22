import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { InventoryService } from '../../services/inventory.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { InventoryItem, getStockStatus } from '../../models/inventory.model';

@Component({
  selector: 'app-inventory-dashboard',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule,
    MatProgressBarModule, MatProgressSpinnerModule, MatTooltipModule, MatDividerModule, MatChipsModule,
    TopbarComponent, BadgeComponent, LoadingSpinnerComponent, PageHeaderComponent, EmptyStateComponent
  ],
  templateUrl: './inventory-dashboard.component.html'
})
export class InventoryDashboardComponent implements OnInit {
  items: InventoryItem[] = [];
  loading = true;
  showInitForm = false;
  initForm = { sku: '', quantity: 100, location: 'WAREHOUSE_MAIN' };
  initLoading = false;
  restockSku = '';
  restockQty = 50;
  showRestock = false;
  restockLoading = false;
  stats = { inStock: 0, lowStock: 0, outOfStock: 0, total: 0 };

  constructor(private inventoryService: InventoryService, private notif: NotificationService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.inventoryService.getAll().subscribe({
      next: (items) => { this.items = items ?? []; this.computeStats(); this.loading = false; },
      error: () => { this.notif.error('Failed to load inventory'); this.loading = false; }
    });
  }

  computeStats() {
    this.stats.total      = this.items.length;
    this.stats.outOfStock = this.items.filter(i => i.availableQuantity === 0).length;
    this.stats.lowStock   = this.items.filter(i => i.availableQuantity > 0 && i.availableQuantity <= i.reorderPoint).length;
    this.stats.inStock    = this.items.length - this.stats.outOfStock - this.stats.lowStock;
  }

  onInitialize() {
    if (!this.initForm.sku) { this.notif.error('SKU is required'); return; }
    this.initLoading = true;
    this.inventoryService.initialize(this.initForm.sku, this.initForm.quantity, this.initForm.location).subscribe({
      next: () => {
        this.notif.success(`Inventory initialized for ${this.initForm.sku}`);
        this.showInitForm = false;
        this.initForm = { sku: '', quantity: 100, location: 'WAREHOUSE_MAIN' };
        this.initLoading = false;
        this.load();
      },
      error: (err) => { this.initLoading = false; this.notif.error(err?.error?.message || 'Initialize failed'); }
    });
  }

  openRestock(sku: string) { this.restockSku = sku; this.restockQty = 50; this.showRestock = true; }

  onRestock() {
    this.restockLoading = true;
    this.inventoryService.restock(this.restockSku, this.restockQty).subscribe({
      next: () => { this.notif.success(`Restocked ${this.restockSku}`); this.showRestock = false; this.restockLoading = false; this.load(); },
      error: () => { this.restockLoading = false; this.notif.error('Restock failed'); }
    });
  }

  stockStatus(item: InventoryItem) { return getStockStatus(item); }
  stockLabel(item: InventoryItem) {
    const s = getStockStatus(item);
    return s === 'in-stock' ? 'In Stock' : s === 'low-stock' ? 'Low Stock' : 'Out of Stock';
  }
  getBadgeType(item: InventoryItem) { return BadgeComponent.forStockStatus(this.stockStatus(item)); }
  stockPct(item: InventoryItem): number { if (!item.quantity) return 0; return Math.min(100, Math.round((item.availableQuantity / item.quantity) * 100)); }
  stockColor(item: InventoryItem): string {
    const s = this.stockStatus(item);
    return s === 'in-stock' ? '#3b82f6' : s === 'low-stock' ? '#f59e0b' : '#ef4444';
  }
}
