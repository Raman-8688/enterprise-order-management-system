import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/auth/services/auth.service';
import { User } from '../../../core/auth/models/user.model';

interface NavItem { label: string; icon: string; route: string; badge?: number; }

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit {
  currentUser: User | null = null;

  mainNav: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard',         route: '/dashboard' },
    { label: 'Orders',    icon: 'shopping_cart',      route: '/orders' },
    { label: 'Products',  icon: 'inventory_2',        route: '/products' },
    { label: 'Inventory', icon: 'warehouse',          route: '/inventory' }
  ];

  financeNav: NavItem[] = [
    { label: 'Payments',  icon: 'credit_card',        route: '/payments' },
    { label: 'Reports',   icon: 'bar_chart',          route: '/reports' }
  ];

  systemNav: NavItem[] = [
    { label: 'Settings',  icon: 'settings',           route: '/settings' }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit() { this.authService.currentUser$.subscribe(u => this.currentUser = u); }

  get userInitials(): string { return this.authService.getUserInitials(); }
  get userDisplayName(): string {
    if (!this.currentUser) return '';
    if (this.currentUser.firstName) return `${this.currentUser.firstName} ${this.currentUser.lastName ?? ''}`.trim();
    return this.currentUser.email;
  }
  get userEmail(): string { return this.currentUser?.email ?? ''; }
  logout() { this.authService.logout(); }
}
