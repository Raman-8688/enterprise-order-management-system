import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/auth/services/auth.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './topbar.component.html'
})
export class TopbarComponent {
  @Input() title = '';
  @Input() subtitle = '';
  constructor(public authService: AuthService) {}
  get initials(): string { return this.authService.getUserInitials(); }
}
