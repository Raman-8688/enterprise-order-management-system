import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Product, CreateProductRequest, ProductStockStatus } from '../models/product.model';
import { Page } from '../../../shared/models/api-response.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private url = environment.productsUrl;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 10, search?: string): Observable<Page<Product>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    return this.http.get<Page<Product>>(this.url, { params });
  }

  getBySku(sku: string): Observable<Product> {
    return this.http.get<Product>(`${this.url}/sku/${sku}`);
  }

  getById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.url}/${id}`);
  }

  getStockStatus(sku: string): Observable<ProductStockStatus> {
    return this.http.get<ProductStockStatus>(`${this.url}/${sku}/stock-status`);
  }

  create(product: CreateProductRequest): Observable<Product> {
    return this.http.post<Product>(this.url, product);
  }

  update(id: string, product: Partial<CreateProductRequest>): Observable<Product> {
    return this.http.put<Product>(`${this.url}/${id}`, product);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  search(query: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.url}/search`, {
      params: new HttpParams().set('query', query)
    });
  }
}
