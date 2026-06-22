import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { AuthService } from '../../../../core/auth/services/auth.service';

@Component({
  selector: 'app-product-create',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatProgressSpinnerModule,
    TopbarComponent, PageHeaderComponent
  ],
  templateUrl: './product-create.component.html'
})
export class ProductCreateComponent {
  loading = false;
  categories = ['Electronics', 'Apparel', 'Furniture', 'Books', 'Sports', 'Home & Kitchen', 'Other'];
  form = { sku: '', name: '', description: '', price: 0, category: '', brand: '', weight: null as number | null, dimensions: '', stockQuantity: 0, createdBy: '' };

  constructor(private productService: ProductService, private notif: NotificationService, private authService: AuthService, private router: Router) {
    this.form.createdBy = this.authService.getCurrentUser()?.email ?? '';
  }

  onSubmit() {
    if (!this.form.sku || !this.form.name || !this.form.price || !this.form.category) { this.notif.error('Please fill in all required fields'); return; }
    this.loading = true;
    const payload = { ...this.form, weight: this.form.weight ?? undefined };
    this.productService.create(payload).subscribe({
      next: () => { this.notif.success('Product created successfully!'); this.router.navigate(['/products']); },
      error: (err) => { this.loading = false; this.notif.error(err?.error?.message || 'Failed to create product'); }
    });
  }
}
