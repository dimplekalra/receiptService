# Receipt Service Architecture

## Overview

The Receipt Service is responsible for ingesting receipt files, extracting structured transaction data, validating monetary consistency and exposing the resulting transaction through REST APIs.

The service is intentionally layered so that responsibilities remain isolated. Each layer has a single purpose, making the application easier to test, maintain and extend.

The current implementation uses a stub OCR service for deterministic testing, while the surrounding architecture is designed so that a production OCR provider can be integrated with minimal changes.

---

# High Level Architecture

```text
                   Client

                      │
                      ▼

             REST Controllers

                      │
                      ▼

            Business Services

      ┌──────────────┼──────────────┐
      │              │              │
      ▼              ▼              ▼

   Storage       OCR Layer      Parser

      │              │              │
      └──────────────┼──────────────┘
                     ▼

          Reconciliation Service

                     │

                     ▼

             Repository Layer

                     │

                     ▼

                 SQLite Database
```

---

# Layer Responsibilities

## Controller Layer

The controller layer is responsible only for HTTP concerns.

Responsibilities include:

- request validation
- invoking services
- returning DTOs
- mapping exceptions to HTTP status codes

Business logic is intentionally excluded from controllers.

---

## Service Layer

The service layer contains all business logic.

Examples include:

- receipt upload
- duplicate detection
- receipt processing
- transaction creation
- reconciliation
- manual item updates

Each service focuses on one responsibility.

Examples:

- ReceiptService
- ReceiptProcessingService
- TransactionUpdateService
- MoneyReconciliationService

---

## Repository Layer

Repositories provide persistence operations.

Spring Data JPA is used to eliminate boilerplate CRUD code.

Repositories never contain business logic.

---

## Storage Layer

The storage layer abstracts physical file storage.

Current implementation:

- Local filesystem

Future implementations may include:

- AWS S3
- Azure Blob Storage
- Google Cloud Storage

Since the application depends only on the StorageService interface, storage providers can be replaced without modifying business logic.

---

## OCR Layer

OCR functionality is abstracted behind the OcrService interface.

Current implementation:

- StubOcrService

Future implementation:

- Google Vision
- AWS Textract
- Azure Document Intelligence
- Tesseract

No service outside the OCR layer depends on a specific OCR provider.

---

## Parsing Layer

ReceiptParser converts OCR text into structured objects.

The parser produces a ParsedReceipt model containing:

- merchant
- date
- currency
- taxes
- line items
- grand total

Separating parsing from persistence allows parser improvements without affecting database logic.

---

# Receipt Processing Flow

```text
Upload Receipt

        │

Validate File

        │

SHA-256 Hash

        │

Duplicate Detection

        │

Store File

        │

Persist Receipt

        │

Process Receipt

        │

OCR

        │

Parse Receipt

        │

Validate Extracted Data

        │

Money Reconciliation

        │

Persist Transaction

        │

Return API Response
```

---

# Duplicate Upload Prevention

Duplicate uploads are prevented using SHA-256 hashing.

For every uploaded file:

1. Calculate SHA-256 hash.
2. Search for an existing receipt using the hash.
3. If found, return the existing receipt.
4. Otherwise store the file and create a new receipt.

This avoids storing multiple copies of identical receipts while allowing idempotent client retries.

---

# Concurrency Strategy

Concurrent processing of the same receipt introduces a race condition.

Example:

```text
Thread A
Process Receipt 10

Thread B
Process Receipt 10
```

Without synchronization both requests could create separate transactions.

The application prevents this using two layers of protection.

## Application-level Lock

Processing uses an in-memory lock keyed by receipt ID.

Only one thread may process a receipt at a time within a single application instance.

This avoids duplicate work and reduces contention.

---

## Database Constraint

The transactions table contains a unique constraint on receipt_id.

Even if multiple application instances process the same receipt concurrently, the database guarantees only one transaction is persisted.

The database remains the final source of truth.

---

# Money Reconciliation

Financial data should never be silently modified.

The reconciliation rule is:

```text
Line Item Total
      +
Tax Total
      =
Grand Total
```

If reconciliation succeeds:

- itemizeStatus = COMPLETE

Otherwise:

- itemizeStatus = NEEDS_REVIEW

The service deliberately avoids adding balancing line items or modifying totals automatically.

This preserves the original financial information extracted from the receipt.

---

# Database Design

## Receipt

Stores uploaded receipt metadata.

Important fields:

- original filename
- stored filename
- SHA-256 hash
- OCR text
- processed flag

---

## ExpenseTransaction

Represents the normalized transaction generated from a receipt.

Contains:

- merchant
- transaction date
- currency
- grand total
- reconciliation status

One receipt maps to exactly one transaction.

---

## LineItem

Stores extracted purchase items.

Each line item belongs to exactly one transaction.

---

## Tax

Stores extracted tax information.

Multiple tax entries are supported to handle receipts containing different tax rates.

---

# Relationships

```text
Receipt

   │ 1

   │

   ▼

ExpenseTransaction

   │ 1

   ├──────────────┐

   ▼              ▼

LineItem(*)     Tax(*)
```

---

# Error Handling

The application returns meaningful HTTP status codes.

Examples include:

| Status | Meaning                |
| ------ | ---------------------- |
| 400    | Invalid request        |
| 404    | Resource not found     |
| 409    | Business rule conflict |
| 422    | Validation failure     |

Business exceptions are converted into consistent API responses.

---

# Design Decisions

## Why SHA-256?

SHA-256 provides deterministic duplicate detection independent of filenames.

Users may upload the same receipt using different filenames while the hash remains identical.

---

## Why BigDecimal?

Currency calculations require exact precision.

Floating-point types such as double introduce rounding errors.

BigDecimal guarantees accurate financial arithmetic.

---

## Why Interfaces?

Major components are hidden behind interfaces.

Examples:

- StorageService
- OcrService
- HashService
- MoneyReconciliationService

This enables dependency inversion and simplifies testing.

---

## Why DTOs?

Entities are never exposed directly through REST APIs.

DTOs provide:

- API stability
- encapsulation
- security
- separation of persistence and transport models

---

## Why Mapper Classes?

Mapping logic is centralized.

Controllers and services remain focused on business logic instead of object transformation.

---

# Testing Strategy

The project emphasizes integration testing.

Current integration tests verify:

- receipt upload
- duplicate detection
- receipt processing
- reconciliation
- transaction retrieval
- transaction updates
- deletion rules
- concurrent processing

The architecture also supports isolated unit testing of services by mocking repository and storage dependencies.

---

# Future Enhancements

The current architecture intentionally leaves room for future improvements.

Potential enhancements include:

- asynchronous receipt processing using a message queue
- cloud object storage
- real OCR providers
- authentication and authorization
- OpenAPI / Swagger documentation
- Docker deployment
- caching
- metrics and observability
- distributed locking for clustered deployments
- optimistic locking for concurrent updates
- pagination and filtering
- audit logging

---

# Summary

The architecture prioritizes:

- clear separation of concerns
- deterministic processing
- financial correctness
- extensibility
- maintainability
- testability

The service is intentionally modular so that individual components such as storage, OCR and parsing can evolve independently without affecting the remainder of the system.
