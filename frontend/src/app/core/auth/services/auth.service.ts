import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { LoginRequest, LoginResponse, User, RegisterRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly AUTH_URL = environment.authUrl;
  private readonly TOKEN_KEY    = 'oms_access_token';
  private readonly REFRESH_KEY  = 'oms_refresh_token';
  private readonly USER_KEY     = 'oms_user';

  private currentUserSubject = new BehaviorSubject<User | null>(this.loadStoredUser());
  public  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  /* ---- Auth Actions ---- */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.AUTH_URL}/login`, credentials).pipe(
      tap(res => {
        if (res.accessToken) {
          this.storeTokens(res.accessToken, res.refreshToken);
          const user: User = { email: res.email, ...this.parseJwtPayload(res.accessToken) };
          this.setCurrentUser(user);
        }
      })
    );
  }

  register(data: RegisterRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.AUTH_URL}/register`, data);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<LoginResponse>(`${this.AUTH_URL}/refresh`, { refreshToken }).pipe(
      tap(res => {
        if (res.accessToken) {
          this.storeTokens(res.accessToken, res.refreshToken);
        }
      })
    );
  }

  /* ---- Token Helpers ---- */
  getToken(): string | null        { return localStorage.getItem(this.TOKEN_KEY); }
  getRefreshToken(): string | null { return localStorage.getItem(this.REFRESH_KEY); }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.parseJwtPayload(token);
      return payload?.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getCurrentUser(): User | null { return this.currentUserSubject.value; }

  getUserInitials(): string {
    const user = this.getCurrentUser();
    if (!user) return 'U';
    if (user.firstName && user.lastName) return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
    return user.email.substring(0, 2).toUpperCase();
  }

  /* ---- Private Helpers ---- */
  private storeTokens(access: string, refresh: string): void {
    localStorage.setItem(this.TOKEN_KEY, access);
    localStorage.setItem(this.REFRESH_KEY, refresh);
  }

  private setCurrentUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private loadStoredUser(): User | null {
    try {
      const stored = localStorage.getItem(this.USER_KEY);
      if (stored) return JSON.parse(stored);
      const token = localStorage.getItem(this.TOKEN_KEY);
      if (token) {
        const payload = this.parseJwtPayload(token);
        if (payload?.exp * 1000 > Date.now()) {
          return { email: payload.sub };
        }
      }
      return null;
    } catch { return null; }
  }

  private parseJwtPayload(token: string): any {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch { return null; }
  }
}
