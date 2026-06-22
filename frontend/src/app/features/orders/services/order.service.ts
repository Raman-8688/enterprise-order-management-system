import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Order, CreateOrderRequest, OrderSummary } from '../models/order.model';
import { Page } from '../../../shared/models/api-response.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private url = environment.ordersUrl;

  constructor(private http: HttpClient) {}

  create(order: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.url, order);
  }

  getById(id: string): Observable<Order> {
    return this.http.get<Order>(`${this.url}/${id}`);
  }

  getByCustomer(email: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.url}/customer/${email}`);
  }

  getByStatus(status: string, page = 0, size = 10): Observable<Page<OrderSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<OrderSummary>>(`${this.url}/status/${status}`, { params });
  }

  getAll(page = 0, size = 10): Observable<Page<Order>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Order>>(this.url, { params });
  }

  cancel(id: string): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/cancel`, {});
  }
}
