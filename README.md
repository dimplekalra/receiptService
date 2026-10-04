# Receipt Service

A Spring Boot REST API for uploading, processing and managing expense receipts.

The service supports receipt upload, duplicate detection, OCR extraction (stub implementation), transaction creation, reconciliation of monetary values and manual correction of line items.

---

## Features

- Upload receipt files
- SHA-256 duplicate detection
- Local file storage
- Stub OCR implementation
- Receipt parsing
- Automatic transaction generation
- Tax extraction
- Line item extraction
- Money reconciliation
- Manual line item correction
- Receipt lifecycle management
- RESTful APIs
- Integration tests

---

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring MVC
- Spring Data JPA
- Hibernate
- SQLite
- Maven
- Lombok
- JUnit 5
- MockMvc

---

## Project Structure

```
src
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── processing
├── ocr
├── hash
├── storage
├── config
├── exception
└── integrationTests
```

---

## Receipt Processing Flow

```
Client

    │

POST /receipts

    │

Validate Upload

    │

SHA-256 Hash

    │

Duplicate Check

    │

Store File

    │

Persist Receipt

    │

POST /receipts/{id}/process

    │

OCR

    │

Receipt Parser

    │

Money Reconciliation

    │

Create Transaction

    │

Persist Taxes

    │

Persist Line Items

    │

Return Transaction
```

---

## Running the Application

Requirements

- Java 21
- Maven

Start the application

```
mvn spring-boot:run
```

The application starts on

```
http://localhost:8080
```

---

## Running Tests

Execute all integration tests

```
mvn test
```

---

## REST APIs

### Receipt APIs

| Method | Endpoint               | Description              |
| ------ | ---------------------- | ------------------------ |
| POST   | /receipts              | Upload receipt           |
| GET    | /receipts/{id}         | Get receipt              |
| DELETE | /receipts/{id}         | Delete receipt           |
| POST   | /receipts/{id}/process | Process uploaded receipt |

---

### Transaction APIs

| Method | Endpoint                      |
| ------ | ----------------------------- |
| GET    | /transactions/{id}            |
| GET    | /transactions?receipt_id={id} |
| PATCH  | /transactions/{id}/line-items |

---

## Duplicate Upload Handling

Duplicate receipts are detected using the SHA-256 hash of the uploaded file.

If the same file is uploaded multiple times, the existing receipt is returned instead of creating a new record.

---

## Money Reconciliation

The application validates that

```
Line Item Total
        +
Tax Total
        =
Grand Total
```

If reconciliation fails

- itemizeStatus becomes NEEDS_REVIEW
- balancing line items are never added automatically
- users may manually update line items through the PATCH endpoint

---

## Error Handling

The application returns appropriate HTTP status codes.

Examples include

- 400 Bad Request
- 404 Not Found
- 409 Conflict
- 422 Unprocessable Entity

---

## Concurrency

The application prevents duplicate processing by combining

- in-memory synchronization
- database uniqueness constraints

This guarantees that concurrent requests cannot create duplicate transactions for the same receipt.

---

## Future Improvements

- Real OCR engine integration
- PDF parsing
- Object storage (AWS S3)
- Authentication & Authorization
- Pagination
- OpenAPI / Swagger
- Docker support
- Background processing using queues
- Metrics and Observability
