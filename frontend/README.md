# Enterprise OMS — Angular Frontend

Production-level Angular 17 frontend for the Enterprise Order Management System microservices backend.

## Tech Stack
| Layer | Technology |
|---|---|
| Framework | Angular 17 (Standalone components, lazy loading) |
| UI Components | Angular Material 17 |
| Layout | Bootstrap 5 (grid only) |
| Icons | Angular Material Icons |
| Styling | Global SCSS + CSS variables |
| HTTP | HttpClient + JWT interceptor + refresh token |
| State | RxJS BehaviorSubject |

## Prerequisites
- Node.js 18+
- Angular CLI: `npm install -g @angular/cli@17`
- Backend running on `http://localhost:8080`

## Setup & Run

```bash
# 1. Navigate to frontend folder
cd frontend

# 2. Install dependencies
npm install

# 3. Start dev server (proxies /api to localhost:8080)
ng serve

# 4. Open browser
http://localhost:4200
```

## Login Credentials (from your backend tests)
```
Email:    gateway@test.com
Password: password123
```

## Project Structure
```
src/app/
├── core/
│   ├── auth/           # JWT auth service, guard, interceptor, login, register
│   └── services/       # Notification (toast) service
├── features/
│   ├── dashboard/      # KPI cards, charts, recent orders
│   ├── products/       # List, create, detail — calls /api/products
│   ├── orders/         # List (master-detail), create, detail — calls /api/orders
│   ├── inventory/      # Dashboard, initialize, restock — calls /api/inventory
│   ├── payments/       # Payment lookup — calls /api/payments
│   ├── reports/        # Analytics tabs with mat-tab
│   └── settings/       # Profile, notifications, API info, security
├── shared/
│   └── components/     # Badge, Topbar, Sidebar, ConfirmDialog, EmptyState, Toast, Spinner
└── layouts/
    └── main-layout/    # Shell with sidebar + router-outlet
```

## Features
- ✅ Login / Register → calls `/api/auth/login` & `/api/auth/register`
- ✅ JWT stored in localStorage, auto-attached via interceptor
- ✅ Auto token refresh on 401
- ✅ Dashboard — live KPIs from all services
- ✅ Products — paginated list, create, detail with stock status
- ✅ Orders — master-detail, create (triggers full Saga), cancel, timeline
- ✅ Inventory — stock level bars, initialize, restock modal
- ✅ Payments — search by order ID, Material table
- ✅ Reports — analytics tabs with charts and service info
- ✅ Settings — profile form, notification toggles, API endpoint list, Kafka info
- ✅ Angular Material throughout (form fields, tables, tabs, chips, toggles, snackbar)
- ✅ Responsive (Bootstrap grid)
- ✅ Global toast notifications
- ✅ Confirm dialogs before destructive actions
- ✅ Lazy-loaded routes for all feature modules

## API Endpoints Used
```
POST /api/auth/login
POST /api/auth/register
GET  /api/products?page=0&size=10
POST /api/products
GET  /api/products/sku/:sku
GET  /api/products/:sku/stock-status
GET  /api/orders?page=0&size=10
POST /api/orders
GET  /api/orders/:id
GET  /api/orders/status/:status
POST /api/orders/:id/cancel
GET  /api/inventory
GET  /api/inventory/:sku
POST /api/inventory/initialize
POST /api/inventory/:sku/restock
GET  /api/payments/order/:orderId
```

## Build for Production
```bash
ng build --configuration production
```
Output in `dist/enterprise-oms-frontend/`
