# 🐳 Docker & Project Running Guide (Low-Storage & Daily Workflow)

This document provides the official instructions for running the **Enterprise Order Management System** cleanly, avoiding Docker storage/memory bloat on Windows, and understanding how automated Kafka event streams operate.

---

## ⚡ 1. Initial / One-Time Setup (First Run or Code Changes)

Use this setup when running the project for the first time or after modifying Java source code in `backend/`.

### Step 1.1: Package Backend JAR Files
Open terminal:
```bash
cd backend
mvn clean package -DskipTests
```

### Step 1.2: Launch Docker Stack
```bash
cd docker
docker-compose up -d --build
```

### Step 1.3: Clean Temporary Build Cache
Immediately after building, prune temporary build layers to prevent disk bloat:
```bash
docker builder prune -f
```

---

## 🚀 2. Daily Workflow (Next Day / Every Day)

> [!IMPORTANT]
> **Golden Rule**: **DO NOT run `docker-compose up --build` or `mvn clean package` every day!** Use existing container images to start the stack in seconds without wasting C: drive storage.

### 🌅 Morning — Start the Application Stack

#### 1. Start Backend Microservices & Infrastructure
Open terminal:
```bash
cd docker
docker-compose up -d
```
*(Starts all 11 containers: Databases, Zookeeper, Kafka, Eureka, API Gateway, and Microservices using existing images & data).*

#### 2. Start Frontend (Angular Client)
Open a second terminal:
```bash
cd frontend
npm start
```

### 🔗 Application URL Dashboard
* **Frontend UI**: [http://localhost:4200](http://localhost:4200)
* **API Gateway Router**: `http://localhost:8080`
* **Eureka Discovery Dashboard**: [http://localhost:8761](http://localhost:8761)
* **Kafka UI Dashboard**: [http://localhost:8086](http://localhost:8086)

---

### 🌆 Evening — Stop the Application Stack
When finished working for the day:
```bash
cd docker
docker-compose stop
```
*(Pauses containers without deleting your PostgreSQL database records).*

---

## 📨 3. How Kafka Events Work (Do You Need to Run Them Manually?)

> [!NOTE]
> **Kafka events run 100% AUTOMATICALLY! You do NOT need to start, run, or rerun Kafka events manually.**

### How Automated Event Flow Operates:
1. When you place an order (`POST /api/orders`), **Order Service** automatically publishes an event to the `order-created-events` Kafka topic.
2. **Payment Service** runs a background listener that automatically detects new messages on `order-created-events`, processes the payment transaction, and publishes a completion event to `payment-processed-events`.
3. **Order Service** automatically listens to `payment-processed-events` and updates the order status to `CONFIRMED` or `CANCELLED`.

### How to Monitor Kafka Events:
* **Kafka UI (Visual)**: Open [http://localhost:8086](http://localhost:8086) in your browser -> Click **Topics** -> Inspect `order-created-events` and `payment-processed-events`.
* **Kafka CLI (Terminal)**:
  ```bash
  docker exec kafka kafka-console-consumer --bootstrap-server kafka:9093 --topic order-created-events --from-beginning
  ```

---

## 🧹 4. Storage Maintenance & Disk Reclaim

### A. Quick Safe Cleanup
```bash
# Deletes dangling layers without touching database volumes
docker system prune -f
```

### B. Reclaim Windows C: Drive Space (WSL 2 Compact)
If Docker's virtual disk (`ext4.vhdx`) expanded on C: drive:
1. Quit Docker Desktop (System Tray -> Quit).
2. Open PowerShell as Administrator:
   ```powershell
   wsl --shutdown
   wsl --compact docker-desktop-data
   ```
