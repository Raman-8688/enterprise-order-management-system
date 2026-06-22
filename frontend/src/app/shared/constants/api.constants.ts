export const API_CONSTANTS = {
  AUTH: {
    LOGIN:    '/auth/login',
    REGISTER: '/auth/register',
    REFRESH:  '/auth/refresh',
    VALIDATE: '/auth/validate',
    USER:     (email: string) => `/auth/user/${email}`
  },
  PRODUCTS: {
    BASE:     '/products',
    BY_SKU:   (sku: string) => `/products/sku/${sku}`,
    SEARCH:   '/products/search',
    STOCK:    (sku: string) => `/products/${sku}/stock-status`
  },
  ORDERS: {
    BASE:        '/orders',
    BY_ID:       (id: string) => `/orders/${id}`,
    BY_CUSTOMER: (email: string) => `/orders/customer/${email}`,
    BY_STATUS:   (status: string) => `/orders/status/${status}`,
    CANCEL:      (id: string) => `/orders/${id}/cancel`
  },
  PAYMENTS: {
    BASE:     '/payments',
    BY_ORDER: (orderId: string) => `/payments/order/${orderId}`
  },
  INVENTORY: {
    BASE:       '/inventory',
    BY_SKU:     (sku: string) => `/inventory/${sku}`,
    INITIALIZE: '/inventory/initialize',
    RESERVE:    (sku: string) => `/inventory/${sku}/reserve`
  }
};
