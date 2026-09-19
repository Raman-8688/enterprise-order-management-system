# Microservices Architecture & MNC Interview Handbook
**Target Audience:** 2+ Years Java / Spring Boot Microservices Developer  
**Project Context:** Enterprise Order Management System (EOMS)  
**Author / Tutor:** Antigravity AI  

---

## 📑 Table of Contents
1. [Architectural Overview & How to Introduce Your Project](#0-how-to-introduce-your-microservices-project-in-interviews)
2. [Module 1: Service Discovery & Registration (Netflix Eureka)](#module-1-service-discovery--registration-netflix-eureka)
3. [Module 2: API Gateway (Spring Cloud Gateway)](#module-2-api-gateway-spring-cloud-gateway)
4. [Module 3: Inter-Service Communication (Spring Cloud OpenFeign)](#module-3-inter-service-communication-spring-cloud-openfeign)
5. [Module 4: Fault Tolerance & Resilience (Resilience4j)](#module-4-fault-tolerance--resilience-resilience4j)
6. [Module 5: Centralized Configuration (Spring Cloud Config Server)](#module-5-centralized-configuration-spring-cloud-config-server)
7. [Module 6: Distributed Tracing & Observability (Micrometer & Zipkin)](#module-6-distributed-tracing--observability-micrometer-tracing--zipkin)
8. [Module 7: Event-Driven Architecture (Apache Kafka & Outbox Pattern)](#module-7-event-driven-architecture-apache-kafka--transactional-outbox)
9. [Quick Revision Summary Cheat Sheet](#9-quick-revision-summary-cheat-sheet)

---

## 0. How to Introduce Your Microservices Project in Interviews

> **Interviewer Question:** *"Can you explain the architecture of your current microservices project?"*

### 🎙️ The 90-Second Model Pitch:
> *"In my current project, the **Enterprise Order Management System (EOMS)**, we designed a distributed, event-driven microservices architecture built on **Java 17/21 and Spring Boot 3**.
>
> 1. **Client Access & Security:** All incoming client traffic enters via **Spring Cloud Gateway**, which acts as our single non-blocking reverse proxy handling JWT validation, CORS, request routing, and rate limiting.
> 2. **Service Discovery:** All microservices (`order-service`, `inventory-service`, `payment-service`, `product-service`) dynamically register with **Netflix Eureka Server**, allowing client-side load balancing without hardcoded IP addresses.
> 3. **Inter-Service Communication:** For synchronous lookups (such as checking stock availability before order placement), services communicate using **Spring Cloud OpenFeign** integrated with **Spring Cloud LoadBalancer**.
> 4. **Resilience & Fault Tolerance:** We use **Resilience4j** to implement Circuit Breaker, Retry, and Fallback patterns to prevent cascading service outages.
> 5. **Asynchronous Processing:** For operations not requiring immediate blocking responses (such as order confirmation emails and analytics), we publish domain events to **Apache Kafka**.
> 6. **Configuration & Observability:** Configuration is centralized via **Spring Cloud Config Server**, while distributed tracing is handled via **Micrometer Tracing and Zipkin** with correlated `Trace ID` and `Span ID` across logs."*

---

## Module 1: Service Discovery & Registration (Netflix Eureka)

### 1. Intuition & Real-World Analogy
* **Problem:** In containerized/cloud environments, service instances scale up and down dynamically. Their IP addresses and ports change continuously. Hardcoding hostnames is unmaintainable.
* **Analogy:** **Truecaller / Phonebook**. You don't remember changing phone numbers; you look up the person's name.
* **Solution:** Services register their network location (`IP:port`) under a logical name (e.g., `order-service`) with Eureka on startup and send periodic heartbeats (every 30s).

---

### 2. MNC Interview Golden Statement
> *"Service Discovery eliminates hardcoded network coordinates by maintaining a real-time registry of all active microservice instances. We use Netflix Eureka where client services register their metadata on startup and send periodic heartbeats. Consumer services query the local registry cache to resolve logical application names to healthy host IP addresses."*

---

### 3. Implementation Blueprint

#### A. Eureka Server (`discovery-server`)
**`pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

**Main Class:**
```java
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
```

**`application.yml`:**
```yaml
server:
  port: 8761
spring:
  application:
    name: discovery-server
eureka:
  client:
    register-with-eureka: false # Server should not register with itself
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### B. Eureka Client (`order-service`)
**`pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**`application.yml`:**
```yaml
server:
  port: 8081
spring:
  application:
    name: order-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

---

### 4. Top MNC Interview Q&A
* **Q: What is Eureka Self-Preservation Mode?**
  * **A:** If Eureka stops receiving heartbeats from >15% of registered instances in a given window (due to a temporary network partition rather than actual instance crashes), it freezes eviction to avoid deleting healthy services from the registry.
* **Q: What happens if Eureka Server goes down completely?**
  * **A:** Client services locally cache the service registry. They can continue routing traffic to known instances until cached data becomes stale or instances move.

---

## Module 2: API Gateway (Spring Cloud Gateway)

### 1. Intuition & Real-World Analogy
* **Problem:** Frontends shouldn't manage 10 different backend service ports. Writing auth, CORS, and logging logic in every service leads to severe code duplication.
* **Analogy:** **Security Front Desk at an Office Building**. Validates your badge (Auth), tells you which elevator to take (Routing), and controls crowd entry (Rate Limiting).

---

### 2. MNC Interview Golden Statement
> *"Spring Cloud Gateway acts as a non-blocking reverse proxy entry point built on Spring WebFlux, Project Reactor, and Netty. It centralizes cross-cutting concerns—such as authentication, authorization, SSL termination, request routing, CORS, and rate limiting—handling high concurrent loads with minimal thread overhead."*

---

### 3. Implementation Blueprint

**`pom.xml`:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

**`application.yml`:**
```yaml
server:
  port: 8080
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: order-service-route
          uri: lb://order-service # 'lb://' queries Eureka & load balances
          predicates:
            - Path=/api/orders/**
          filters:
            - AddRequestHeader=X-Gateway-Source, EOMS-Gateway
```

#### Custom Global Authentication Filter:
```java
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        if (request.getURI().getPath().contains("/api/auth/")) {
            return chain.filter(exchange);
        }
        
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Highest priority
    }
}
```

---

### 4. Top MNC Interview Q&A
* **Q: Why Spring Cloud Gateway instead of Netflix Zuul 1.x?**
  * **A:** Zuul 1.x uses blocking I/O (one thread per connection using Servlet API). Spring Cloud Gateway uses non-blocking reactive streams (Netty), significantly increasing throughput under heavy concurrency.
* **Q: What is the difference between Pre-filter and Post-filter?**
  * **A:** Pre-filter runs before forwarding the request (`chain.filter(exchange)`). Post-filter runs after downstream responds (`.then(Mono.fromRunnable(...))`).

---

## Module 3: Inter-Service Communication (Spring Cloud OpenFeign)

### 1. Intuition & Real-World Analogy
* **Problem:** `RestTemplate` requires manual URL building, manual deserialization, and repetitive error handling.
* **Analogy:** Calling a standard local Java method instead of dialing a telephone manually.
* **Solution:** Declare an interface with standard Spring MVC annotations (`@GetMapping`, `@RequestParam`); Spring Cloud generates the HTTP client dynamically.

---

### 2. MNC Interview Golden Statement
> *"Spring Cloud OpenFeign is a declarative HTTP client. By defining Java interfaces with Spring MVC annotations, we eliminate boilerplate REST calling code. It natively integrates with Eureka and Spring Cloud LoadBalancer to provide client-side load balancing and supports RequestInterceptors for passing security tokens across services."*

---

### 3. Implementation Blueprint

**`pom.xml` in `order-service`:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

**Enable Feign on Main Class:**
```java
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication { ... }
```

**Declarative Feign Client Interface:**
```java
@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/api/inventory/check-stock")
    List<InventoryResponse> isInStock(@RequestParam("skuCodes") List<String> skuCodes);
}
```

#### JWT Propagation Across Feign Calls (`RequestInterceptor`):
```java
@Configuration
public class FeignAuthInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String token = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (token != null) {
                template.header(HttpHeaders.AUTHORIZATION, token);
            }
        }
    }
}
```

---

### 4. Top MNC Interview Q&A
* **Q: How does Feign know which IP:port to call?**
  * **A:** Feign takes the `name = "inventory-service"`, asks Spring Cloud LoadBalancer to look up the Eureka cache, picks one IP using Round-Robin, and executes the HTTP call.
* **Q: How do you configure timeouts for Feign?**
  * **A:** Via `spring.cloud.openfeign.client.config.default.connectTimeout: 5000` and `readTimeout: 5000` in `application.yml`.

---

## Module 4: Fault Tolerance & Resilience (Resilience4j)

### 1. Intuition & Real-World Analogy
* **Problem (Cascading Failures):** When downstream `inventory-service` hangs, incoming `order-service` threads get blocked waiting. All Tomcat worker threads get exhausted, crashing the entire service.
* **Analogy:** **House MCB / Electrical Circuit Breaker**. Trips during a short-circuit to save the entire house from catching fire.

---

### 2. MNC Interview Golden Statement
> *"Resilience4j is a lightweight, functional fault tolerance library that replaces Netflix Hystrix. We use it to implement Circuit Breaker, Retry, Rate Limiter, and Fallbacks. It monitors call failure rates over a sliding window; when failures exceed a threshold, it trips to OPEN state to fail-fast and execute fallback logic without exhausting server thread pools."*

---

### 3. Circuit Breaker State Machine

```
[ CLOSED ] --(Failure rate > 50%)--> [ OPEN ]
    ^                                   |
    |                                (Wait 10s)
    |                                   v
    +------(Trial calls pass)------- [ HALF-OPEN ]
```

---

### 4. Implementation Blueprint

**`pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**`application.yml`:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      inventoryCB:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000ms
        permittedNumberOfCallsInHalfOpenState: 3
```

**Java Service with Fallback:**
```java
@Service
@Slf4j
public class OrderService {

    @CircuitBreaker(name = "inventoryCB", fallbackMethod = "inventoryFallback")
    public String placeOrder(List<String> skuCodes) {
        // HTTP call to inventory service...
        return "Order Placed Successfully";
    }

    // ⚠️ FALLBACK RULES: Same return type + Same parameters + Throwable at the end!
    public String inventoryFallback(List<String> skuCodes, Throwable throwable) {
        log.error("Inventory unavailable. Root cause: {}", throwable.getMessage());
        return "Inventory service is currently busy. Your order is queued for retry.";
    }
}
```

---

### 5. Top MNC Interview Q&A
* **Q: What are the strict rules for writing a Fallback method?**
  * **A:** (1) Same return type, (2) Same parameter list, (3) Must add `Throwable` or specific exception as the last argument, (4) Must reside in the same class.
* **Q: What is the order of execution between `@Retry` and `@CircuitBreaker`?**
  * **A:** `@Retry` wraps `@CircuitBreaker`. Retry will re-attempt transient failures first; only if all retries fail is it counted as a failure by CircuitBreaker.

---

## Module 5: Centralized Configuration (Spring Cloud Config Server)

### 1. Intuition & Real-World Analogy
* **Problem:** If you have 50 microservice instances and you change a feature flag or database password, rebuilding and redeploying 50 JARs causes downtime.
* **Analogy:** **Central Digital Notice Board** instead of posting physical letters to every single apartment.

---

### 2. MNC Interview Golden Statement
> *"Spring Cloud Config Server provides externalized, centralized configuration management backed by Git or Vault. Microservices fetch their environment-specific configurations during startup. By annotating beans with `@RefreshScope` and invoking `/actuator/refresh` or using Spring Cloud Bus with Kafka, we reload configurations dynamically at runtime without restarting application instances."*

---

### 3. Implementation Blueprint

#### Config Server:
**`pom.xml`:** `spring-cloud-config-server`  
**Main Class:** `@EnableConfigServer`  
**`application.yml`:**
```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/my-org/eoms-config-repo.git
```

#### Config Client (`order-service`):
**`application.yml` (Spring Boot 3.x Standard):**
```yaml
spring:
  application:
    name: order-service
  profiles:
    active: dev
  config:
    import: "optional:configserver:http://localhost:8888"

management:
  endpoints:
    web:
      exposure:
        include: refresh, health
```

**Controller with `@RefreshScope`:**
```java
@RestController
@RefreshScope
public class ConfigTestController {
    @Value("${app.discount.percentage:0}")
    private int discount;

    @GetMapping("/discount")
    public String getDiscount() {
        return "Current Discount: " + discount + "%";
    }
}
```

---

### 4. Top MNC Interview Q&A
* **Q: How does `@RefreshScope` work internally?**
  * **A:** Spring wraps `@RefreshScope` beans in a CGLIB proxy. When `/actuator/refresh` is triggered, Spring clears the target bean instance from cache. On the next request, the proxy creates a fresh bean instance initialized with the updated properties.
* **Q: How do you refresh 50 instances at once?**
  * **A:** Using **Spring Cloud Bus** connected to Kafka/RabbitMQ. Sending a single `POST /actuator/busrefresh` broadcasts the refresh event across all running instances.

---

## Module 6: Distributed Tracing & Observability (Micrometer Tracing & Zipkin)

### 1. Intuition & Real-World Analogy
* **Problem:** In a request chain `Gateway -> Order -> Inventory -> Payment`, if a request takes 5s or throws an error, finding the culprit in 4 separate log files is impossible.
* **Analogy:** **Courier Tracking Number**. Every leg of the journey shares the same tracking number.

---

### 2. MNC Interview Golden Statement
> *"In Spring Boot 3, Micrometer Tracing (replacing deprecated Spring Cloud Sleuth) injects a globally unique `Trace ID` across the entire distributed transaction and a unique `Span ID` for each local unit of work. These IDs are propagated across HTTP headers and logged in MDC format, allowing us to visualize latency bottlenecks and exceptions in Zipkin."*

---

### 3. Trace ID vs. Span ID Breakdown
* **Trace ID (`a67c4e829bf10c3b`):** Shared across ALL services for a single end-to-end user request.
* **Span ID (`8f1291880e431a47`):** Unique to a single service operation or hop.

---

### 4. Implementation Blueprint

**`pom.xml`:**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-tracing-bridge-brave</artifactId>
    </dependency>
    <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>zipkin-reporter-brave</artifactId>
    </dependency>
</dependencies>
```

**`application.yml`:**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0 # (Use 0.05 or 0.1 in high-traffic PROD)
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

---

## Module 7: Event-Driven Architecture (Apache Kafka & Transactional Outbox)

### 1. Intuition & Real-World Analogy
* **Problem:** When placing an order, sending emails or pushing analytics synchronously blocks the user and causes order failure if the email server is down.
* **Analogy:** **Fast Food Kitchen Order Ticket**. Cashier gives you an order token and puts the ticket on the counter. Kitchen cooks independently.

---

### 2. MNC Interview Golden Statement
> *"We use Apache Kafka for asynchronous, event-driven decoupling. When an order is created, `order-service` publishes an `OrderPlacedEvent` to a Kafka topic. Downstream consumers (`notification-service`, `analytics-service`) process events independently in their own consumer groups, guaranteeing resilience and high throughput."*

---

### 3. Implementation Blueprint

**Producer (`order-service`):**
```java
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void publishEvent(OrderPlacedEvent event) {
        // Key ensures all events for the same order stay in the same partition (guarantees ordering)
        kafkaTemplate.send("order-placed-topic", event.getOrderNumber(), event);
    }
}
```

**Consumer (`notification-service`):**
```java
@Service
@Slf4j
public class NotificationListener {
    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group")
    public void handleOrderEvent(OrderPlacedEvent event) {
        log.info("Processing notification for Order: {}", event.getOrderNumber());
        // Send email/SMS...
    }
}
```

---

### 4. Advanced Production Patterns

#### 1. The Transactional Outbox Pattern (Fixes Dual-Write Problem)
* **Problem:** If database commit succeeds but Kafka publish fails, events are lost.
* **Solution:** Save the entity and event payload in a local DB `outbox` table in the **same DB transaction**. A CDC tool (**Debezium**) reads DB commit logs and reliably publishes to Kafka.

#### 2. Message Ordering Guarantee
* Kafka guarantees message ordering **only within a single partition**.
* Use a consistent **Message Key** (e.g., `orderNumber`) so all events for that order hit the same partition.

---

## 9. Quick Revision Summary Cheat Sheet

| Microservices Topic | Primary Tool | Key Annotation / Header | Core Interview Pitch Keyword |
| :--- | :--- | :--- | :--- |
| **Service Discovery** | Netflix Eureka | `@EnableEurekaServer` / Client | Dynamic IP resolution, Heartbeats, Self-preservation |
| **API Gateway** | Spring Cloud Gateway | `GlobalFilter`, `lb://` | Non-blocking Netty, Central Auth/Routing/Rate-limiting |
| **Inter-Service** | OpenFeign | `@FeignClient`, `RequestInterceptor` | Declarative HTTP, client-side load balancing, token propagation |
| **Fault Tolerance** | Resilience4j | `@CircuitBreaker`, `@Retry` | Prevent cascading failure, CLOSED $\to$ OPEN $\to$ HALF-OPEN |
| **Central Config** | Config Server | `@RefreshScope`, `/actuator/refresh` | Zero-downtime updates, Git repo backend, Spring Cloud Bus |
| **Distributed Tracing**| Micrometer + Zipkin | `Trace ID`, `Span ID`, Brave | MDC log correlation, latency bottleneck discovery |
| **Event-Driven** | Apache Kafka | `@KafkaListener`, `KafkaTemplate` | Asynchronous decoupling, Partition keys, Outbox pattern |

---
*Happy Learning & Best of Luck for your MNC Technical Interviews!* 🚀
