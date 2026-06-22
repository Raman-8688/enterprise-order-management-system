import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Product, ProductStockStatus } from '../../models/product.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule, MatButtonModule, MatDividerModule, TopbarComponent, BadgeComponent, LoadingSpinnerComponent, PageHeaderComponent],
  templateUrl: './product-detail.component.html'
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  stockStatus: ProductStockStatus | null = null;
  loading = true;

  constructor(private route: ActivatedRoute, private productService: ProductService, private notif: NotificationService) {}

  ngOnInit() {
    const sku = this.route.snapshot.paramMap.get('sku')!;
    forkJoin({
      product: this.productService.getBySku(sku),
      stock:   this.productService.getStockStatus(sku).pipe(catchError(() => of(null)))
    }).subscribe({
      next: ({ product, stock }) => { this.product = product; this.stockStatus = stock; this.loading = false; },
      error: () => { this.notif.error('Product not found'); this.loading = false; }
    });
  }
}
