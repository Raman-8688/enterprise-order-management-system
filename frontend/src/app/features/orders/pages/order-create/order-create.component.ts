import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';
import { OrderService } from '../../services/order.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { MatSelectModule } from '@angular/material/select';
import { Product } from 'src/app/features/products/models/product.model';
import { ProductService } from 'src/app/features/products/services/product.service';


interface CartItem { productSku: string; quantity: number; }

@Component({
  selector: 'app-order-create',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,MatSelectModule,
    MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule, MatDividerModule,
    TopbarComponent, PageHeaderComponent, BadgeComponent
  ],
  templateUrl: './order-create.component.html'
})
export class OrderCreateComponent  implements OnInit {
  loading = false;
  form = { customerEmail: '', customerName: '', shippingAddress: '' };
  items: CartItem[] = [{ productSku: '', quantity: 1 }];
  result: any = null;
  availableProducts: Product[] = [];

  constructor(private orderService: OrderService, 
    private notif: NotificationService, private router: Router,
     private productService: ProductService,
  ) {}


   ngOnInit() {
    this.loadProducts(); // Load products on init
  }

  loadProducts() {
    this.productService.getAll(0, 100).subscribe({
      next: (page) => {
        this.availableProducts = page.content || [];
      },
      error: (err) => {
        console.error('Failed to load products:', err);
      }
    });
  }

  addItem() { this.items.push({ productSku: '', quantity: 1 }); }
  removeItem(i: number) { if (this.items.length > 1) this.items.splice(i, 1); }

  // onSubmit() {
  //   if (!this.form.customerEmail || !this.form.customerName || !this.form.shippingAddress) { this.notif.error('Please fill in all customer details'); return; }
  //   if (this.items.some(it => !it.productSku || it.quantity < 1)) { this.notif.error('Please fill in all item details'); return; }
  //   this.loading = true;
  //   this.orderService.create({ ...this.form, items: this.items }).subscribe({
  //     next: (order) => { this.loading = false; this.result = order; this.notif.success(`Order ${order.orderNumber} — ${order.status}`); },
  //     error: (err) => { this.loading = false; this.notif.error(err?.error?.message || 'Order creation failed'); }
  //   });
  // }
  onSubmit() {
    if (!this.form.customerEmail || !this.form.customerName || !this.form.shippingAddress) {
      this.notif.error('Please fill in all customer details');
      return;
    }
    
    // Validate all items have valid SKU
    for (const item of this.items) {
      if (!item.productSku || item.productSku.trim() === '') {
        this.notif.error('Please select a product for all items');
        return;
      }
      if (item.quantity < 1) {
        this.notif.error('Quantity must be at least 1');
        return;
      }
    }
    
    this.loading = true;
    this.orderService.create({ ...this.form, items: this.items }).subscribe({
      next: (order) => {
        this.loading = false;
        this.result = order;
        this.notif.success(`Order ${order.orderNumber} — ${order.status}`);
      },
      error: (err) => {
        this.loading = false;
        this.notif.error(err?.error?.message || 'Order creation failed');
      }
    });
  }


  reset() { this.result = null; this.form = { customerEmail: '', customerName: '', shippingAddress: '' }; this.items = [{ productSku: '', quantity: 1 }]; }
  viewOrder() { this.router.navigate(['/orders', this.result.id]); }
  getBadgeType(s: string) { return BadgeComponent.forOrderStatus(s); }
}
