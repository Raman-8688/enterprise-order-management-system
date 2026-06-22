export interface OrderItem {
  productSku: string;
  productName?: string;
  quantity: number;
  unitPrice?: number;
  subtotal?: number;
}

export type OrderStatus =
  | 'PENDING' | 'CONFIRMED' | 'PROCESSING'
  | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'FAILED';

export interface Order {
  id: string;
  orderNumber: string;
  customerEmail: string;
  customerName: string;
  shippingAddress: string;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  paymentId?: string;
  failureReason?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderSummary {
  id: string;
  orderNumber: string;
  totalAmount: number;
  status: OrderStatus;
  createdAt?: string;
}

export interface CreateOrderRequest {
  customerEmail: string;
  customerName: string;
  shippingAddress: string;
  items: { productSku: string; quantity: number }[];
}
