# m5-pub — tax calculator and in-memory order API

A small Java 17 teaching repository containing a simplified Indian
income-tax/GST calculator and a four-endpoint order API backed by an
in-memory map, built with `javac` and a Makefile (no build framework).

## Description

Two independent pieces of code sit under `src/`:

- **`TaxCalculator`** — computes old-regime income tax over three
  simplified slabs (2.5L / 5L / 10L), GST at the five statutory rates
  (0, 5, 12, 18, 28), a flat 80C-style exemption capped at 1.5L,
  rounding to paise, and an age-banded return-filing eligibility check.
- **`OrderApi`** — a `@RestController` exposing four endpoints over
  `OrderService`, which stores orders in a `ConcurrentHashMap`. The
  Spring annotations are local stubs in `SpringStubs.java`; Spring
  itself is **not** on the classpath and there is no embedded server,
  so the controller compiles and can be read and documented, but is
  not deployed by this repository.

### Endpoints

| Method   | Path           | Success | Errors                      |
|----------|----------------|---------|-----------------------------|
| `GET`    | `/orders`      | 200     | —                           |
| `GET`    | `/orders/{id}` | 200     | 404 unknown id              |
| `POST`   | `/orders`      | 201     | 400 invalid body            |
| `DELETE` | `/orders/{id}` | 204     | 409 unknown/already cancelled |

## Build

```sh
make deps      # download JUnit 5, JaCoCo and PIT into libs/
make build     # compile src/ and test/ into build/
make test      # run the JUnit 5 suite
make coverage  # JaCoCo HTML report at coverage/index.html
make mutation  # PIT HTML report at build/reports/pitest/index.html
make clean     # remove build/, libs/, coverage/ and jacoco.exec
```

Requires a JDK supporting `--release 17` and network access on the first
`make deps` (jars are fetched from Maven Central).

## Quick example

```java
TaxCalculator calc = new TaxCalculator();

// Income tax on INR 7,00,000: 5% on 2.5L-5L, then 20% on 5L-7L
BigDecimal tax = calc.computeIncomeTax(new BigDecimal("700000"));  // 52500.00

// GST at 18% on an invoice of INR 1,000
BigDecimal gst = calc.computeVAT(new BigDecimal("1000"), 18);      // 180.00

// A 65-year-old must file if gross income exceeds INR 3,00,000
boolean mustFile = calc.isEligibleForReturn(new BigDecimal("310000"), 65);  // true
```

## Contributing

1. Run `make test` before and after your change; the suite must stay green.
2. Run `make coverage` and keep `TaxCalculator` line coverage at or above
   its current level.
3. Run `make mutation` and check that your new tests kill mutants rather
   than merely covering lines — a test that raises coverage without
   raising the mutation score is usually asserting too little.
4. Write tests in AAA form with one assertion per test, named
   `<method>_<condition>_<expected>`.

## License

MIT License (placeholder — no `LICENSE` file is committed in this
repository yet).

---

## Module 5 exercise notes (from the original starter README)

Session 5A: use an LLM to raise `TaxCalculator` line/branch coverage
above the ~45%/~30% baseline the three seed tests give you.
Session 5B: add JavaDoc, a README, and an OpenAPI spec for `OrderApi`.

The `mutation` target uses PIT 1.17.4 with the JUnit 5 plugin; both are
auto-downloaded into `libs/` on first use. Baseline mutation score with
the seed tests is around 36% (15 of 42 mutants killed).
