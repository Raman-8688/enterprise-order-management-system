export interface Product {
  id: string;
  sku: string;
  name: string;
  description?: string;
  price: number;
  category: string;
  brand?: string;
  weight?: number;
  dimensions?: string;
  stockQuantity?: number;
  active: boolean;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductStockStatus {
  sku: string;
  productName: string;
  productActive: boolean;
  inventoryAvailable: boolean;
  availableQuantity: number;
  message: string;
  circuitBreakerOpen: boolean;
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  description?: string;
  price: number;
  category: string;
  brand?: string;
  weight?: number;
  dimensions?: string;
  stockQuantity?: number;
  createdBy?: string;
}
