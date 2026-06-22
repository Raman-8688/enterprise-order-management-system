import { Routes } from '@angular/router';
import { authGuard } from './core/auth/guards/auth.guard';
import { MainLayoutComponent } from './layouts/main-layout.component';

export const routes: Routes = [
  // Auth routes (no layout, no guard)
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./core/auth/pages/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./core/auth/pages/register/register.component').then(m => m.RegisterComponent)
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  // Protected routes (inside main layout with sidebar)
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

      // Dashboard
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },

      // Products
      {
        path: 'products',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/products/pages/product-list/product-list.component').then(m => m.ProductListComponent)
          },
          {
            path: 'create',
            loadComponent: () =>
              import('./features/products/pages/product-create/product-create.component').then(m => m.ProductCreateComponent)
          },
          {
            path: ':sku',
            loadComponent: () =>
              import('./features/products/pages/product-detail/product-detail.component').then(m => m.ProductDetailComponent)
          }
        ]
      },

      // Orders
      {
        path: 'orders',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/orders/pages/order-list/order-list.component').then(m => m.OrderListComponent)
          },
          {
            path: 'create',
            loadComponent: () =>
              import('./features/orders/pages/order-create/order-create.component').then(m => m.OrderCreateComponent)
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./features/orders/pages/order-detail/order-detail.component').then(m => m.OrderDetailComponent)
          }
        ]
      },

      // Inventory
      {
        path: 'inventory',
        loadComponent: () =>
          import('./features/inventory/pages/inventory-dashboard/inventory-dashboard.component').then(m => m.InventoryDashboardComponent)
      },

      // Payments
      {
        path: 'payments',
        loadComponent: () =>
          import('./features/payments/pages/payment-list/payment-list.component').then(m => m.PaymentListComponent)
      },

      // Reports
      {
        path: 'reports',
        loadComponent: () =>
          import('./features/reports/pages/reports/reports.component').then(m => m.ReportsComponent)
      },

      // Settings
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/settings/pages/settings/settings.component').then(m => m.SettingsComponent)
      }
    ]
  },

  // Catch-all
  { path: '**', redirectTo: '/dashboard' }
];
