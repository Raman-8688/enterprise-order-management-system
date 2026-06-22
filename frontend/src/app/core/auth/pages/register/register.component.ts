import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatFormFieldModule, MatInputModule
  ],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  firstName = ''; lastName = ''; email = ''; password = '';
  loading = false; showPass = false; error = '';

  constructor(private authService: AuthService, private notif: NotificationService, private router: Router) {}

  onSubmit() {
    if (!this.firstName || !this.lastName || !this.email || !this.password) { this.error = 'All fields are required.'; return; }
    if (this.password.length < 6) { this.error = 'Password must be at least 6 characters.'; return; }
    this.error = ''; this.loading = true;
    this.authService.register({ email: this.email, password: this.password, firstName: this.firstName, lastName: this.lastName }).subscribe({
      next: () => { this.notif.success('Account created! Please log in.'); this.router.navigate(['/auth/login']); },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Registration failed. Please try again.'; }
    });
  }
}
