import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TopbarComponent } from '../../../../shared/components/topbar/topbar.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { User } from '../../../../core/auth/models/user.model';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatSlideToggleModule, MatDividerModule,
    MatSnackBarModule, MatTabsModule, MatChipsModule, MatTooltipModule,
    TopbarComponent, PageHeaderComponent
  ],
  templateUrl: './settings.component.html'
})
export class SettingsComponent implements OnInit {
  user: User | null = null;
  saving = false;

  profile = { firstName: '', lastName: '', email: '', phone: '', department: '', timezone: 'Asia/Kolkata' };

  notifications = {
    orderAlerts:    true,
    lowStockAlerts: true,
    paymentAlerts:  false,
    kafkaEvents:    true,
    emailDigest:    false
  };

  notifPrefs = [
    { key: 'orderAlerts',    icon: 'shopping_cart',  label: 'Order alerts',          desc: 'Notified when new orders are placed or status changes' },
    { key: 'lowStockAlerts', icon: 'warning',         label: 'Low stock alerts',      desc: 'Alert when inventory falls below reorder threshold' },
    { key: 'paymentAlerts',  icon: 'credit_card',     label: 'Payment notifications', desc: 'Receive success and failure payment alerts' },
    { key: 'kafkaEvents',    icon: 'bolt',            label: 'Kafka event logs',      desc: 'Log all Kafka events in the notification panel' },
    { key: 'emailDigest',    icon: 'email',           label: 'Daily email digest',    desc: 'Receive a daily summary report via email' }
  ];

  endpoints = [
    { label: 'Auth Service',     url: 'http://localhost:8080/api/auth',      method: 'POST/GET' },
    { label: 'Products',         url: 'http://localhost:8080/api/products',  method: 'GET/POST' },
    { label: 'Orders',           url: 'http://localhost:8080/api/orders',    method: 'GET/POST' },
    { label: 'Inventory',        url: 'http://localhost:8080/api/inventory', method: 'GET/POST' },
    { label: 'Payments',         url: 'http://localhost:8080/api/payments',  method: 'GET' },
    { label: 'Eureka Dashboard', url: 'http://localhost:8761',               method: 'GET' }
  ];

  kafkaTopics = [
    { name: 'order-created-events',        desc: 'Published when a new order is confirmed', partitions: 3 },
    { name: 'order-status-changed-events', desc: 'Published on every order status update',  partitions: 3 }
  ];

  services = [
    { name: 'API Gateway',          port: 8080, desc: 'Routes requests, validates JWT',        color: '#3b82f6', icon: 'router' },
    { name: 'Auth Service',          port: 8081, desc: 'JWT generation & user management',      color: '#16a34a', icon: 'security' },
    { name: 'Product Service',       port: 8082, desc: 'Product catalogue, Feign + CB',         color: '#7c3aed', icon: 'inventory_2' },
    { name: 'Inventory Service',     port: 8083, desc: 'Stock tracking & reservation',          color: '#d97706', icon: 'warehouse' },
    { name: 'Order Service',         port: 8084, desc: 'Saga orchestrator, Kafka producer',     color: '#dc2626', icon: 'shopping_cart' },
    { name: 'Payment Service',       port: 8085, desc: 'Payment processing via Saga',           color: '#0891b2', icon: 'credit_card' },
    { name: 'Notification Service',  port: 8086, desc: 'Kafka consumer, email/SMS alerts',      color: '#059669', icon: 'notifications' },
    { name: 'Eureka Discovery',      port: 8761, desc: 'Service registry & health monitoring',  color: '#92400e', icon: 'hub' }
  ];

  timezones = ['Asia/Kolkata', 'UTC', 'America/New_York', 'Europe/London', 'Asia/Singapore'];

  sessionInfo = [
    { label: 'Auth method',   value: 'JWT (HS512)' },
    { label: 'Token expires', value: '24 hours' },
    { label: 'Refresh token', value: '7 days' }
  ];

  constructor(
    private authService: AuthService,
    private notif: NotificationService,
    private snack: MatSnackBar
  ) {}

  ngOnInit() {
    this.user = this.authService.getCurrentUser();
    if (this.user) {
      this.profile.firstName = this.user.firstName ?? '';
      this.profile.lastName  = this.user.lastName  ?? '';
      this.profile.email     = this.user.email     ?? '';
    }
  }

  get initials(): string { return this.authService.getUserInitials(); }

  getToggleValue(key: string): boolean {
    return this.notifications[key as keyof typeof this.notifications];
  }

  setToggleValue(key: string, value: boolean): void {
    (this.notifications as any)[key] = value;
  }

  saveProfile() {
    this.saving = true;
    setTimeout(() => { this.saving = false; this.notif.success('Profile updated successfully'); }, 800);
  }

  saveNotifications() { this.notif.success('Notification preferences saved'); }

  logout() { this.authService.logout(); }

  copyUrl(url: string) {
    navigator.clipboard.writeText(url).then(() => {
      this.snack.open('URL copied!', '', { duration: 2000 });
    });
  }
}
