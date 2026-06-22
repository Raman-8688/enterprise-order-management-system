export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
export type PaymentMethod = 'CREDIT_CARD' | 'DEBIT_CARD' | 'UPI' | 'NET_BANKING' | 'WALLET';

export interface Payment {
  id: string;
  paymentId: string;
  orderId: string;
  orderNumber?: string;
  customerEmail?: string;
  amount: number;
  status: PaymentStatus;
  method?: PaymentMethod;
  transactionId?: string;
  failureReason?: string;
  createdAt?: string;
  updatedAt?: string;
}
