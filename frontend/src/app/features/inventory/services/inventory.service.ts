import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { InventoryItem } from '../models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private url = environment.inventoryUrl;

  constructor(private http: HttpClient) {}

  getBySku(sku: string): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.url}/${sku}`);
  }

  initialize(sku: string, quantity: number, location: string): Observable<InventoryItem> {
    const params = new HttpParams()
      .set('sku', sku)
      .set('quantity', quantity)
      .set('location', location);
    return this.http.post<InventoryItem>(`${this.url}/initialize`, null, { params });
  }

  restock(sku: string, quantity: number): Observable<InventoryItem> {
    const params = new HttpParams().set('quantity', quantity);
    return this.http.post<InventoryItem>(`${this.url}/${sku}/restock`, null, { params });
  }

  getAll(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(this.url);
  }
}
