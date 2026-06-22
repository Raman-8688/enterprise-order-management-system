export interface ApiResponse<T = any> {
  data?: T;
  message?: string;
  status: number;
  timestamp?: string;
  path?: string;
  error?: string;
  validationErrors?: Record<string, string>;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
