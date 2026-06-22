export interface User {
  id?: string;
  email: string;
  firstName?: string;
  lastName?: string;
  name?: string;
  role?: UserRole;
  createdAt?: string;
  updatedAt?: string;
}

export enum UserRole {
  ADMIN   = 'ADMIN',
  USER    = 'USER',
  MANAGER = 'MANAGER'
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  message: string;
  success: boolean;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role?: UserRole;
}

export interface ValidateTokenResponse {
  valid: boolean;
  email: string;
  message: string;
}
