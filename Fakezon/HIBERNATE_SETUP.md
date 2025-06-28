# Hibernate Persistence Setup

This document describes the Hibernate persistence implementation added to the Fakezon system, covering both Product and Store entities.

## Overview

The system now uses Hibernate JPA for persistence instead of in-memory storage. This provides:
- Data persistence across application restarts
- Advanced querying capabilities
- Transaction management
- Database independence

## Components Added

### 1. Dependencies (pom.xml)
- `spring-boot-starter-data-jpa`: Spring Data JPA with Hibernate
- `h2`: H2 in-memory database for development/testing
- `postgresql`: PostgreSQL driver for production

### 2. Entity Classes

#### Product Entity
- **`DomainLayer.Model.Product`**: Updated with JPA annotations
  - `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
  - `@Column` annotations for field constraints
  - `@Enumerated` for category enum mapping
  - `@ElementCollection` for store relationships

#### Store Entity
- **`DomainLayer.Model.Store`**: Updated with JPA annotations
  - `@Entity`, `@Table`, `@Id` 
  - `@OneToMany` relationships for ratings and products
  - `@ElementCollection` for store owners and pending owners
  - `@Transient` for complex business logic fields (temporarily excluded)

#### Supporting Entities
- **`DomainLayer.Model.StoreRating`**: JPA entity for store ratings
- **`DomainLayer.Model.ProductRating`**: JPA entity for product ratings  
- **`DomainLayer.Model.StoreProduct`**: JPA entity for store products

### 3. Repository Layer

#### Spring Data JPA Repositories
- **`InfrastructureLayer.ProductJpaRepository`**: Product-specific queries
- **`InfrastructureLayer.StoreJpaRepository`**: Store-specific queries

#### Repository Implementations
- **`InfrastructureLayer.ProductRepositoryImpl`**: Implements `IProductRepository`
- **`InfrastructureLayer.StoreRepositoryImpl`**: Implements `IStoreRepository`

### 4. Configuration
- **`ApplicationLayer.Services.JpaConfig`**: Enables JPA repositories and entity scanning
- **`src/main/resources/application.properties`**: Database configuration
- **`src/test/resources/application-test.properties`**: Test-specific configuration

## Database Configuration

### Production (PostgreSQL - Neon)
```properties
spring.datasource.url=jdbc:postgresql://ep-white-resonance-a446eyl4-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require
spring.datasource.username=neondb_owner
spring.datasource.password=npg_6J7ZqvcrbSuH
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Development/Testing (H2)
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

## Database Schema

### Tables Created
1. **`products`**: Core product information
2. **`product_stores`**: Product-store relationships
3. **`stores`**: Core store information  
4. **`store_ratings`**: Store ratings by users
5. **`product_ratings`**: Product ratings by users
6. **`store_products`**: Products within stores
7. **`store_owners`**: Store ownership relationships
8. **`pending_owners`**: Pending store ownership assignments

## Testing

### Integration Tests
- **`IntegrationTesting.ProductPersistenceTest`**: ✅ All tests passing
- **`IntegrationTesting.StorePersistenceTest`**: ✅ All tests passing

### Test Coverage
- Entity creation and retrieval
- Search operations
- Data validation
- Relationship mapping
- Delete operations

## Key Implementation Notes

### 1. Collection Mapping Strategy
- Used `Map<Integer, Entity>` interfaces instead of `HashMap<Integer, Entity>` 
- This allows Hibernate to properly manage persistent collections

### 2. Relationship Mappings
- **One-to-Many**: Store → StoreRating, Store → StoreProduct
- **Element Collections**: Store owners, pending owners, product stores

### 3. Transient Fields
- Complex business logic objects marked as `@Transient`
- Event publishers, locks, and schedulers excluded from persistence
- Can be added incrementally as needed

### 4. ID Generation
- **Products**: Auto-generated identity strategy
- **Stores**: Manual ID assignment for UI compatibility
- **Ratings**: Auto-generated identity strategy

## Performance Considerations

### 1. Lazy Loading
- Collections configured with `FetchType.LAZY`
- Minimizes initial query overhead
- Loads related data only when accessed

### 2. Query Optimization
- Custom JPQL queries for complex operations
- Native SQL for performance-critical operations (e.g., top-rated stores)

### 3. Transaction Management
- `@Transactional` annotations for proper transaction boundaries
- Automatic rollback on exceptions

## Next Steps

### 1. User Entity Persistence
- Add JPA annotations to User entity
- Create UserJpaRepository and implementation
- Handle user authentication and session management

### 2. Order Entity Persistence  
- Add JPA annotations to Order entity
- Handle order-product relationships
- Implement order history and tracking

### 3. Advanced Features
- Add full-text search capabilities
- Implement caching strategies
- Add database migration scripts
- Set up database connection pooling

### 4. Production Readiness
- Configure proper PostgreSQL connection pooling
- Set up database backup strategies
- Implement monitoring and logging
- Add performance metrics

## Usage Examples

### Creating a Product
```java
Product product = new Product("iPhone 15", "Latest smartphone", PCategory.ELECTRONICS);
productRepository.addProduct(product);
```

### Creating a Store
```java
Store store = new Store("Tech Store", 123, eventPublisher);
storeRepository.addStore(store);
```

### Querying
```java
// Find products by category
Collection<IProduct> electronics = productRepository.getProductsByCategory(PCategory.ELECTRONICS);

// Search stores
Collection<Store> results = storeRepository.searchStores("tech");

// Get top-rated stores
Collection<Store> topStores = storeRepository.getTop10Stores();
```

## Success Metrics

✅ **PostgreSQL Integration**: Connected to Neon cloud database  
✅ **Entity Persistence**: Product and Store entities fully persisted  
✅ **Relationship Mapping**: Complex relationships properly mapped  
✅ **Test Coverage**: Comprehensive integration tests passing  
✅ **Performance**: Lazy loading and query optimization implemented  
✅ **Transaction Safety**: Proper transaction management in place 