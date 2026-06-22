import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { EmptyStateComponent } from '../../../../shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatIconModule, MatButtonModule, MatTooltipModule, MatInputModule, MatFormFieldModule, MatChipsModule,
    TopbarComponent, BadgeComponent, LoadingSpinnerComponent,
    ConfirmDialogComponent, EmptyStateComponent, PageHeaderComponent
  ],
  templateUrl: './product-list.component.html'
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  loading  = true;
  total    = 0;
  page     = 0;
  size     = 10;
  search   = '';
  filter   = 'ALL';
  deleteId = '';
  showConfirm = false;
  filters = ['ALL', 'Electronics', 'Apparel', 'Furniture'];

  constructor(private productService: ProductService, private notif: NotificationService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.productService.getAll(this.page, this.size, this.search || undefined).subscribe({
      next: (page) => { this.products = page.content ?? []; this.total = page.totalElements ?? 0; this.loading = false; },
      error: () => { this.notif.error('Failed to load products'); this.loading = false; }
    });
  }

  onSearch() { this.page = 0; this.load(); }
  onFilter(f: string) { this.filter = f; this.page = 0; this.load(); }
  goToPage(p: number) { this.page = p; this.load(); }
  confirmDelete(id: string) { this.deleteId = id; this.showConfirm = true; }

  doDelete() {
    this.showConfirm = false;
    this.productService.delete(this.deleteId).subscribe({
      next: () => { this.notif.success('Product deleted'); this.load(); },
      error: () => this.notif.error('Delete failed')
    });
  }

  get totalPages() { return Math.ceil(this.total / this.size); }
  get pages() { return Array.from({ length: Math.min(this.totalPages, 5) }, (_, i) => i); }

  getStockBadge(product: Product): { label: string; type: any } {
    if (!product.active) return { label: 'Inactive', type: 'muted' };
    return { label: 'Active', type: 'success' };
  }
}
