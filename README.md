Wallet REST API

High-throughput Wallet REST API built with Java 17, Spring Boot 3.2, PostgreSQL, Liquibase, and Docker.

Designed to safely handle 1000 RPS per wallet using proper concurrency control and transactional consistency.

📌 Features

Deposit and Withdraw operations

Automatic wallet creation on first deposit

Retrieve wallet balance

Pessimistic locking (SELECT ... FOR UPDATE)

Liquibase database migrations

Dockerized application and PostgreSQL database

Environment-based configuration (no rebuild required)

Integration test coverage

Consistent error response format

No 50X errors under concurrent load

🏗 Tech Stack

Java 17

Spring Boot 3.2

Spring Data JPA

PostgreSQL

Liquibase

HikariCP (Connection Pool)

Docker & Docker Compose

JUnit 5

📡 API Endpoints
1️⃣ Execute Wallet Operation
POST /api/v1/wallet

Content-Type: application/json

Request Body
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 1000
}
Request Fields
Field	Type	Description
walletId	UUID	Unique wallet identifier
operationType	String	DEPOSIT or WITHDRAW
amount	Integer	Positive transaction amount
Business Rules

First DEPOSIT creates the wallet automatically.

WITHDRAW for non-existent wallet → 404 Not Found

Insufficient funds → 422 Unprocessable Entity

Amount must be positive

2️⃣ Get Wallet Balance
GET /api/v1/wallets/{WALLET_UUID}
Response
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1000
}
❌ Error Response Format

All errors follow this structure:

{
  "error": "Short title",
  "message": "Detailed message",
  "path": "/api/v1/...",
  "status": 400,
  "timestamp": "2026-02-28T12:00:00Z"
}
Error Cases
Scenario	HTTP Status
Wallet not found	404
Insufficient funds	422
Invalid JSON	400
Validation error	400
⚡ Concurrency Strategy

To support 1000 RPS per wallet safely:

Pessimistic locking (SELECT ... FOR UPDATE)

Short-lived transactions

HikariCP connection pool tuning

Strict service-layer transactional boundaries

No unprocessed requests (no 50X errors)

This prevents race conditions and double spending.

🐳 Running with Docker
Build and Start
docker compose up -d --build
View Logs
docker compose logs -f app

API runs at:

http://localhost:8080
Stop Containers
docker compose down

Remove database volume:

docker compose down -v
⚙ Configuration (No Rebuild Required)

Configure using environment variables or .env.

Variable	Default	Description
POSTGRES_DB	wallet_db	Database name
POSTGRES_USER	wallet_user	Database user
POSTGRES_PASSWORD	wallet_pass	Database password
POSTGRES_PORT	5432	DB port
SERVER_PORT	8080	Application port
DB_POOL_SIZE	50	HikariCP max pool size
Example .env
POSTGRES_PASSWORD=secret
SERVER_PORT=9090
DB_POOL_SIZE=100

Then run:

docker compose up -d
💻 Run Locally (Without Docker)

Start PostgreSQL (localhost:5432)

Create database:

createdb -U wallet_user wallet_db

Run application:

./mvnw spring-boot:run
🧪 Tests

Requires JDK 17.

./mvnw test

Uses H2 in-memory database

Integration tests cover endpoints

🗄 Database

Managed with Liquibase migrations.

Automatic schema creation

Version-controlled changesets

Database history tracking

📂 Project Structure
src/main/java
 ├── controller
 ├── service
 ├── repository
 ├── entity
 └── config

src/main/resources
 └── db/changelog
👨‍💻 Author

Naveen Kuratti
Backend Developer | Java | Spring Boot
