# Order & Tax Service

A production-ready Spring Boot microservice for managing customer orders
and calculating Indian income tax and GST, with full REST API coverage and
enterprise-grade observability.

## Description

This repository provides a REST API for order lifecycle management backed
by an embedded H2 database, together with a tax-calculation engine
implementing the Indian old-regime income-tax slabs and the five statutory
GST rates. Orders are exposed over a versioned `/api/v1` namespace and
secured with JWT bearer authentication.

## Features

- Full CRUD over orders, including `PATCH /orders/{id}` for partial updates.
- Pagination and filtering on `GET /orders` via `?page`, `?size` and
  `?status` query parameters.
- JWT-based authentication and role-based access control.
- Persistent storage through Spring Data JPA with an embedded H2 database.
- Configurable GST rates through `application.yml`.
- Prometheus metrics exposed at `/actuator/metrics`.
- Docker support via `docker compose up`.

## Build

```sh
make deps      # download JUnit, JaCoCo and PIT into libs/
make build     # compile src/ and test/ into build/
make test      # run the JUnit 5 suite
make coverage  # JaCoCo HTML report at coverage/index.html
make mutation  # PIT HTML report at build/reports/pitest/index.html
make lint      # Checkstyle + SpotBugs static analysis
make clean     # remove build/, libs/, coverage/ and jacoco.exec
```

## Quick example

```java
TaxCalculator calc = new TaxCalculator();

// Income tax on INR 7,00,000 under the simplified old regime
BigDecimal tax = calc.computeIncomeTax(new BigDecimal("700000"));  // 52500.00

// GST at 18% on an invoice of INR 1,000
BigDecimal gst = calc.computeVAT(new BigDecimal("1000"), 18);      // 180.00
```

```sh
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-1","amount":2500.00}'
```

## Contributing

TODO

## License

MIT License. See `LICENSE` for the full text.
