import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatFormFieldModule, MatInputModule
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = ''; password = ''; loading = false; showPass = false; error = '';

  constructor(
    private authService: AuthService,
    private notif: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    if (this.authService.isAuthenticated()) this.router.navigate(['/dashboard']);
  }

  onSubmit() {
    if (!this.email || !this.password) { this.error = 'Please enter your email and password.'; return; }
    this.error = ''; this.loading = true;
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.notif.success('Welcome back!');
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Invalid credentials. Please try again.'; }
    });
  }
}
