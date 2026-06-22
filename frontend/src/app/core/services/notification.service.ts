import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  success(message: string, duration = 3500) { this.show('success', message, duration); }
  error  (message: string, duration = 4500) { this.show('error',   message, duration); }
  warning(message: string, duration = 4000) { this.show('warning', message, duration); }
  info   (message: string, duration = 3500) { this.show('info',    message, duration); }

  private show(type: Toast['type'], message: string, duration: number) {
    const toast: Toast = { id: Date.now().toString(), type, message, duration };
    this.toastsSubject.next([...this.toastsSubject.value, toast]);
    setTimeout(() => this.dismiss(toast.id), duration);
  }

  dismiss(id: string) {
    this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
  }
}
