export interface InventoryItem {
  sku: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  available: boolean;
  message?: string;
  location: string;
  minimumStockLevel: number;
  reorderPoint: number;
  needsReorder: boolean;
  lastRestockedAt?: string;
  updatedAt?: string;
}

export type StockStatus = 'in-stock' | 'low-stock' | 'out-of-stock';

export function getStockStatus(item: InventoryItem): StockStatus {
  if (item.availableQuantity === 0) return 'out-of-stock';
  if (item.availableQuantity <= item.reorderPoint) return 'low-stock';
  return 'in-stock';
}
