# Backend Setup

## Requirements
- Java 8+
- Maven 3.8+
- MySQL 8.0+

## Database Setup

```sql
CREATE DATABASE `full-stack-ecommerce`;
CREATE USER 'ecommerceapp'@'localhost' IDENTIFIED BY 'ecommerceapp';
GRANT ALL PRIVILEGES ON `full-stack-ecommerce`.* TO 'ecommerceapp'@'localhost';
FLUSH PRIVILEGES;
```

Or use the setup script:
```bash
mysql -u root -p < database_setup.sql
```

## Build & Run

```bash
cd spring-boot-ecommerce

# Build
mvn clean package

# Run tests (optional)
mvn test

# Start
mvn spring-boot:run
```

App runs on `http://localhost:8080`

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/full-stack-ecommerce
spring.datasource.username=ecommerceapp  
spring.datasource.password=ecommerceapp

app.jwtSecret=mySecretKey123
stripe.api.secret-key=sk_test_KEY
stripe.api.publishable-key=pk_test_KEY
```

## API Endpoints

**Auth:**
- POST /api/auth/register
- POST /api/auth/login
- GET /api/auth/me

**Products:**
- GET /api/products
- GET /api/product-categories

**Cart:**
- GET /api/cart
- POST /api/cart/add-item
- DELETE /api/cart/remove-item/{id}

**Orders:**
- POST /api/orders/create
- GET /api/orders

**Payment:**
- POST /api/payment/process

## Troubleshooting

**MySQL connection error:**
```bash
mysql -u root -p -e "SELECT VERSION();"
```

**Port 8080 in use:**
Edit `application.properties` and change `server.port=8081`

**Tests fail:**
```bash
mvn clean test
```

## Notes
- Change JWT secret before production
- Update Stripe keys (live keys for prod)
- Use different DB credentials  
- Enable HTTPS in production

