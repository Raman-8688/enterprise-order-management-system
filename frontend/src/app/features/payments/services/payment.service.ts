import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Payment } from '../models/payment.model';
import { Page } from '../../../shared/models/api-response.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private url = environment.paymentsUrl;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 10): Observable<Payment[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Payment[]>(this.url, { params });
  }

  getById(id: string): Observable<Payment> {
    return this.http.get<Payment>(`${this.url}/${id}`);
  }

  getByOrderId(orderId: string): Observable<Payment> {
    return this.http.get<Payment>(`${this.url}/order/${orderId}`);
  }
}
