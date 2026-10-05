# AI Session Log — M5 (Testing and Documentation with AI)

Repo: https://github.com/Sajjan-Naveen-87/m5-pub

Starter: `m5-pub.tgz`
Toolchain: Temurin JDK 25 (`javac --release 17`), JUnit 5.10.0,
JaCoCo 0.8.11, PIT 1.17.4 + pitest-junit5-plugin 1.2.2.

## Note on the starter contents

**Prompt given to AI:**
> extract the tgz and check everything that the handout says should be
> in there is actually there before we start

**What the AI did:** Listed the archive and compared it against the
"Starter contents" list in the handout. Four of the five source files
were present (`TaxCalculator.java`, `Order.java`, `OrderDto.java`,
`OrderService.java`) plus `TaxCalculatorTest.java` and the `Makefile`.
**`OrderApi.java` is missing from `m5-pub.tgz`** even though both the
handout and the starter's own `README.md` refer to it ("fake Spring
annotations already in place so it compiles without Spring on the
classpath").

Since Session 5B is entirely about documenting `OrderApi`, I had the AI
reconstruct it from the three things that do pin it down: the handout's
description ("Spring `@RestController` with four endpoints"), the
`OrderService` API it has to sit on top of (`listAll`, `findById`,
`create`, `cancel`), and `OrderDto.from(Order)`. The annotation stubs
the starter README promised were written as `src/SpringStubs.java`.

Files added to make 5B possible: `src/OrderApi.java`,
`src/SpringStubs.java`, `src/ResponseStatusException.java`,
`src/CreateOrderRequest.java`. Everything builds with plain `javac`;
Spring is still not on the classpath.

---

# Session 5A — AI-generated tests

## Part A — Baseline coverage

**Prompt given to AI:**
> run make coverage on the clean starter and pull the real numbers out
> of the jacoco xml, per method, i don't want to eyeball the html

**What the AI did:** Ran `make deps && make test` (3 seed tests, all
green), then `make coverage`, then parsed `coverage/report.xml` rather
than reading percentages off the HTML, so the per-method line/branch
counters are exact.

```
=== BASELINE (3 seed tests) ===
  <init>                 LINE 1/1=100%     BRANCH n/a
  computeIncomeTax       LINE 10/17=59%    BRANCH 5/10=50%
  computeVAT             LINE 4/6=67%      BRANCH 6/14=43%
  applyExemption         LINE 5/6=83%      BRANCH 4/8=50%
  roundToPaise           LINE 0/3=0%       BRANCH 0/2=0%
  isEligibleForReturn    LINE 0/8=0%       BRANCH 0/10=0%
  TOTAL LINE:   20/41 = 48.8%
  TOTAL BRANCH: 15/44 = 34.1%
```

**Line coverage: 48.8%** (20/41) on `TaxCalculator`.
**Branch coverage: 34.1%** (15/44) on `TaxCalculator`.

(Across all five classes in `build/` the figures are 39.8% line and
31.2% branch, but `TaxCalculator` is the class under test and the one
the handout's ~45%/~30% refers to.)

**Methods with < 50% branch coverage:**

| Method | Branch | Why |
|---|---|---|
| `isEligibleForReturn` | **0%** (0/10) | no seed test touches it at all |
| `roundToPaise` | **0%** (0/2) | no seed test touches it at all |
| `computeVAT` | **43%** (6/14) | seed only passes rate 18, so four of the five rate checks never evaluate |

`computeIncomeTax` and `applyExemption` sit exactly at 50%, so they are
not under the threshold.

**Result:** Baseline matches the handout's stated ~45%/~30%.
`isEligibleForReturn` is the method to attack — it is tied at 0% with
`roundToPaise` but has 10 uncovered branches to `roundToPaise`'s 2, so
it is where the branch coverage actually is.

## Part B — Generate tests with constraints

**Prompt given to AI:** (the handout's prompt, with the chosen method's
source pasted in)
> Generate JUnit 5 tests for the following method.
> Rules:
> - Use AAA style with explicit comments.
> - One assertion per test.
> - Test names of the form
>   `<method>_<condition>_<expected>`.
> - Cover: (a) null input, (b) empty input if applicable,
>   (c) boundary values, (d) one representative happy path.
> - Do NOT use mocks.
> - Do NOT include implementation-copying assertions; each
>   assertion must state a contract, not restate the code.
>
> ```java
> public boolean isEligibleForReturn(BigDecimal grossIncome, int ageYears) {
>     if (grossIncome == null || ageYears < 0) {
>         return false;
>     }
>     BigDecimal threshold;
>     if (ageYears >= 80) {
>         threshold = new BigDecimal("500000");
>     } else if (ageYears >= 60) {
>         threshold = new BigDecimal("300000");
>     } else {
>         threshold = new BigDecimal("250000");
>     }
>     return grossIncome.compareTo(threshold) > 0;
> }
> ```

**What the AI replied:** 15 tests. It read the method as the
age-banded filing threshold it is (general / senior 60+ / super-senior
80+) and built the boundary cases in pairs — on the threshold, and one
rupee over it — plus a pair that pins which band each age falls into
(59 and 79). It treated `BigDecimal.ZERO` as the "empty input"
analogue, since the parameter is a number and not a collection.

The 15 tests it produced:

| # | Test name | Case |
|---|---|---|
| 1 | `isEligibleForReturn_nullGrossIncome_returnsFalse` | null input |
| 2 | `isEligibleForReturn_negativeAge_returnsFalse` | guard |
| 3 | `isEligibleForReturn_zeroIncome_returnsFalse` | "empty" input |
| 4 | `isEligibleForReturn_generalAgeIncomeAtThreshold_returnsFalse` | boundary 250000 |
| 5 | `isEligibleForReturn_generalAgeIncomeOneRupeeOverThreshold_returnsTrue` | boundary 250001 |
| 6 | `isEligibleForReturn_ageFiftyNine_usesGeneralThreshold` | age boundary 59 |
| 7 | `isEligibleForReturn_seniorAtSixtyIncomeAtThreshold_returnsFalse` | boundary 300000 @ 60 |
| 8 | `isEligibleForReturn_seniorAtSixtyIncomeOneRupeeOverThreshold_returnsTrue` | boundary 300001 @ 60 |
| 9 | `isEligibleForReturn_ageSeventyNine_usesSeniorThreshold` | age boundary 79 |
| 10 | `isEligibleForReturn_superSeniorAtEightyIncomeAtThreshold_returnsFalse` | boundary 500000 @ 80 |
| 11 | `isEligibleForReturn_superSeniorAtEightyIncomeOneRupeeOverThreshold_returnsTrue` | boundary 500001 @ 80 |
| 12 | `isEligibleForReturn_ageZero_usesGeneralThreshold` | age boundary 0 |
| 13 | `isEligibleForReturn_salariedWellAboveThreshold_returnsTrue` | happy path |
| 14 | `isEligibleForReturn_generalAge_matchesThresholdComparison` | — |
| 15 | `isEligibleForReturn_incomeEqualToThreshold_comparisonIsNotPositive` | — |

Representative shape of the good ones:

```java
@Test
public void isEligibleForReturn_seniorAtSixtyIncomeAtThreshold_returnsFalse() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act
    boolean eligible = c.isEligibleForReturn(new BigDecimal("300000"), 60);
    // Assert
    assertFalse(eligible);
}
```

Pasted all 15 into `test/TaxCalculatorTest.java` and ran `make test`:

```
[        18 tests found           ]
[         0 tests skipped         ]
[        18 tests started         ]
[         0 tests aborted         ]
[        18 tests successful      ]
[         0 tests failed          ]
```

**Result:** 18/18 green (3 seed + 15 generated). Note that **all 15
passed, including the two that Part C then throws out** — which is the
whole point of Part C: passing is not the same as being worth keeping.

## Part C — Read every assertion

Went through all 15 assertions one at a time asking the handout's
question: does this encode a contract, or does it restate the code?
**13 pass. 2 fail, and both were deleted.**

**Deleted #1 — `isEligibleForReturn_generalAge_matchesThresholdComparison`**

```java
BigDecimal threshold = age >= 80 ? new BigDecimal("500000")
                     : age >= 60 ? new BigDecimal("300000")
                     : new BigDecimal("250000");
boolean eligible = c.isEligibleForReturn(income, age);
assertEquals(income.compareTo(threshold) > 0, eligible);
```

This is exactly the pitfall the handout names. The expected value is
re-derived from the input using the same slab ladder and the same
`compareTo(...) > 0` that the method body uses. It would pass against
*any* refactor of the method and also against several wrong ones — if
somebody flipped the whole ladder, the "expected" would flip with it.
It asserts the implementation, not the contract.

**Deleted #2 — `isEligibleForReturn_incomeEqualToThreshold_comparisonIsNotPositive`**

```java
BigDecimal income    = new BigDecimal("250000");
BigDecimal threshold = new BigDecimal("250000");
int comparison = income.compareTo(threshold);
assertFalse(comparison > 0);
```

Worse than tautological — it never calls `isEligibleForReturn` at all.
It is a test of `java.math.BigDecimal.compareTo`, which is the JDK's
job to test. Zero information about our code.

The 13 kept assertions all state a domain fact that would survive a
rewrite of the method: *"a 60-year-old on exactly 3,00,000 does not
have to file"*, *"a 59-year-old is judged against the general
threshold, not the senior one"*, and so on. None of them recomputes
the answer from the input.

After deleting the two:

```
[        16 tests found           ]
[        16 tests successful      ]
[         0 tests failed          ]
```

Coverage after Part B + C:

```
  isEligibleForReturn    LINE 8/8=100%     BRANCH 10/10=100%
  TOTAL LINE:   28/41 = 68.3%   (was 48.8%)
  TOTAL BRANCH: 25/44 = 56.8%   (was 34.1%)
```

**Result:** Target method taken from 0% to **100% line and 100%
branch**, and the two worthless tests are gone. Deleting them cost
nothing in coverage — further evidence they were carrying no weight.

## Part D — Mutation testing

**Prompt given to AI:**
> run make mutation and give me the per-method breakdown with the
> surviving mutants listed, the html report is painful to read

**What the AI did:** Ran `make mutation`. PIT's XML output turned out
to be unusable here — `--outputFormats=XML` dies with
`ClassNotFoundException: org.apache.commons.text.StringEscapeUtils`,
because the four PIT jars the `Makefile` downloads don't bundle
commons-text. So the AI parsed `build/reports/pitest/default/TaxCalculator.java.html`
instead and grouped the mutants by method.

**Baseline mutation score (seed tests only): 15/42 = 36%** — matching
the figure the starter `README.md` predicts.

```
### computeIncomeTax:    killed  6/13   {KILLED: 6, SURVIVED: 4, NO_COVERAGE: 3}
### computeVAT:          killed  4/9    {KILLED: 4, SURVIVED: 4, NO_COVERAGE: 1}
### applyExemption:      killed  5/7    {KILLED: 5, SURVIVED: 2}
### roundToPaise:        killed  0/2    {NO_COVERAGE: 2}
### isEligibleForReturn: killed  0/11   {NO_COVERAGE: 11}
TOTAL killed 15/42 = 36%
```

**For the chosen method (`isEligibleForReturn`), after the Part B/C
tests:**

```
### isEligibleForReturn: killed 11/11   {KILLED: 11}
   L70  KILLED  changed conditional boundary
   L70  KILLED  negated conditional
   L70  KILLED  negated conditional
   L71  KILLED  replaced boolean return with true
   L74  KILLED  changed conditional boundary
   L74  KILLED  negated conditional
   L76  KILLED  changed conditional boundary
   L76  KILLED  negated conditional
   L81  KILLED  changed conditional boundary
   L81  KILLED  negated conditional
   L81  KILLED  replaced boolean return with true
```

**Mutation score for the chosen method: 11/11 = 100%. Surviving
mutants: none.** The paired at-threshold / one-rupee-over tests are
what kill the three `changed conditional boundary` mutants, and the
age-59 / age-79 tests kill the boundary mutants on the `>= 60` and
`>= 80` band checks. Whole-class score went 15/42 → **26/42 = 62%**.

Because the chosen method had nothing left to kill, I took the
surviving mutant from the next-weakest method, `computeVAT` (4/9),
where three `negated conditional` mutants on the GST-rate guard
survived:

```java
// TaxCalculator.java:41
if (gstRatePercent != 0 && gstRatePercent != 5 && gstRatePercent != 12
        && gstRatePercent != 18 && gstRatePercent != 28) {
    throw new IllegalArgumentException("unsupported GST rate: " + gstRatePercent);
}
```

The seed test only ever passes `18`. At `18` the chain short-circuits
on the fourth clause, so the first three clauses are evaluated but
their outcome never changes the result — negate any of them and the
test still passes.

**Prompt given to AI:** (the handout's prompt, filled in)
> The mutant at line 41 (`gstRatePercent != 0` -> `gstRatePercent == 0`)
> survived the test suite. Write ONE new JUnit test that would kill this
> mutant without changing any other test. Explain in one sentence why
> this test kills the mutant.

**What the AI replied:**

```java
@Test
public void computeVAT_zeroRatedSupply_returnsNoTax() {
    // Arrange
    TaxCalculator c = new TaxCalculator();
    // Act -- 0% is a supported GST rate (zero-rated supply), not an error
    BigDecimal gst = c.computeVAT(new BigDecimal("1000"), 0);
    // Assert
    assertEquals(0, gst.compareTo(BigDecimal.ZERO));
}
```

> Why it kills the mutant: rate 0 is the only input for which the first
> clause decides the outcome — the original `0 != 0` is false and
> short-circuits the whole guard so the method returns 0.00, whereas the
> mutated `0 == 0` is true and lets the chain run on to throw
> `IllegalArgumentException`, so the test goes from passing to erroring.

Added it and re-ran PIT:

```
### computeVAT: killed 5/9   {KILLED: 5, SURVIVED: 3, NO_COVERAGE: 1}
   L41  KILLED       negated conditional     <-- was SURVIVED
   L41  KILLED       negated conditional
   L41  NO_COVERAGE  negated conditional
   L41  SURVIVED     negated conditional
   L41  SURVIVED     negated conditional
```

```
>> Line Coverage (for mutated classes only): 28/41 (68%)
>> Generated 42 mutations Killed 27 (64%)
>> Mutations with no coverage 6. Test strength 75%
```

**Result:** Mutant killed, confirmed by re-running PIT.
`computeVAT` 4/9 → 5/9, whole class 26/42 → **27/42 = 64%**. The test
is also worth keeping on its own merits — a zero-rated supply really is
legal under GST, and nothing in the suite asserted that before.

## Part E — Reflect

**Line and branch coverage after the changes, vs baseline:**

| | Baseline | Final | Change |
|---|---|---|---|
| Line (`TaxCalculator`) | 48.8% (20/41) | **68.3%** (28/41) | +19.5 pts |
| Branch (`TaxCalculator`) | 34.1% (15/44) | **59.1%** (26/44) | +25.0 pts |
| `isEligibleForReturn` line | 0% (0/8) | **100%** (8/8) | +100 |
| `isEligibleForReturn` branch | 0% (0/10) | **100%** (10/10) | +100 |
| `computeVAT` branch | 42.9% (6/14) | **50%** (7/14) | +7.1 pts |

**Mutation score before and after the targeted test:**

| Stage | Score |
|---|---|
| Seed tests only | 15/42 = **36%** |
| After Part B + C (13 kept tests) | 26/42 = **62%** |
| After the Part D targeted test | 27/42 = **64%** |
| `isEligibleForReturn` alone | 0/11 → **11/11 = 100%** |

**Was the mutation feedback more or less useful than the coverage
feedback?**

More useful, and the clearest proof of it is the two tests Part C threw
out: both raised nothing and both passed, yet JaCoCo would happily have
counted the lines they touched, whereas PIT recorded no change in
killed mutants when they were deleted — the mutation score could see
they were empty and the coverage number could not. The sharper example
is `computeVAT`: JaCoCo rated it 43% branch and it looked like a
middling problem, but PIT showed that four of its nine mutants survived
*even on lines the seed test executes*, because short-circuit
evaluation means a clause can be run without its result ever mattering.
Coverage told me which lines I had not visited; mutation testing told
me which assertions were not actually load-bearing — and only the
second one distinguishes a test from a line of exercise.

---

# Session 5B — Auto-documentation

## Part A — JavaDoc for one endpoint

Chose `POST /orders` → `OrderApi.createOrder(CreateOrderRequest)`.

**Prompt given to AI:** (handout prompt, with the method body pasted)
> Generate JavaDoc for the following method. Rules:
> - One-line summary in imperative mood.
> - Describe the contract, not the implementation.
> - @param for each parameter with type and constraints.
> - @return with what is returned.
> - @throws for each declared or unchecked exception the
>   caller should be aware of.
> - If any behaviour is not evident from the code, write
>   "TODO" and skip.

**What the AI replied (first pass):**

```java
/**
 * Create a new order for a customer.
 *
 * <p>Validates the incoming request, persists the order with an initial
 * status of {@code PENDING}, and returns the created resource. Responds
 * with HTTP 201 Created on success.
 *
 * @param request the order to create; must be non-null, with a non-blank
 *                {@code customerId} and a strictly positive {@code amount}
 * @return an {@link OrderDto} describing the newly created order, including
 *         its server-generated UUID and its creation timestamp
 * @throws ResponseStatusException with status 400 if the request is null,
 *         if {@code customerId} is null or blank, or if {@code amount} is
 *         null or negative
 * @throws ResponseStatusException with status 409 if an order already
 *         exists for the same customer
 */
```

**The verification step.**

**Prompt given to AI:**
> Given the JavaDoc above and the method body below,
> identify any statement in the JavaDoc that is inconsistent
> with the code. Do not fix; just list.

**What the AI replied — 4 inconsistencies on the first pass:**

1. **"initial status of `PENDING`"** — `Order`'s constructor sets
   `this.status = "NEW"` (`Order.java:15`). There is no `PENDING`
   anywhere in the repository.
2. **"a strictly positive `amount`"** — the guard is
   `amount.signum() < 0` (`OrderApi.java:36`), which rejects only
   negatives. An amount of exactly `0` is accepted, so the constraint
   is "zero or greater", not "strictly positive".
3. **"including ... its creation timestamp"** — `OrderDto` is
   `(String id, String customerId, BigDecimal amount, String status)`
   (`OrderDto.java:3`). `Order` does hold a `createdAt`, but the DTO
   returned to the caller does not carry it.
4. **"`@throws` ... 409 if an order already exists for the same
   customer"** — `createOrder` has no duplicate check and throws no
   409. The only 409 in the file is in `cancelOrder`
   (`OrderApi.java:46`), for a different condition entirely.

I confirmed each of the four by grepping the source rather than taking
the AI's word for it (`grep -n 'this.status' src/Order.java`,
`grep -n 'signum' src/OrderApi.java`, `grep -n 'record OrderDto'
src/OrderDto.java`, `grep -n '409' src/OrderApi.java`). All four held
up.

**Corrected JavaDoc, now in `src/OrderApi.java`:**

```java
/**
 * Create a new order for a customer.
 *
 * <p>The order is stored under a server-generated identifier and starts
 * in the {@code NEW} status. Responds with HTTP 201 Created on success.
 *
 * @param request the order to create; must be non-null, must carry a
 *                non-blank {@code customerId}, and must carry a non-null
 *                {@code amount} of zero or greater
 * @return an {@link OrderDto} describing the newly created order: its
 *         generated id, the customer id, the amount, and the status
 * @throws ResponseStatusException with status 400 if {@code request} is
 *         null, if {@code customerId} is null or blank, or if
 *         {@code amount} is null or negative
 */
```

Re-ran the verification prompt against the corrected version: **"no
inconsistencies"**. Two passes total.

**Result:** JavaDoc on `createOrder` that describes the contract and
survives its own cross-check. Three of the four original errors were
*plausible-sounding inventions* — `PENDING` instead of `NEW`, a
timestamp that exists on the entity but not the DTO, and a duplicate-
customer 409 borrowed from the neighbouring method. All three are the
kind of thing a reviewer skims straight past.

## Part B — Draft the README

**Prompt given to AI:** (handout prompt, with the file tree and the
top-level files pasted)
> Draft a README.md for this repository with sections:
> description, build, quick example, contributing, license.
> Use MIT license placeholder.
> Rules:
> - Do NOT invent features not present in the code.
> - If a section has no evidence in the code, write "TODO" and
>   skip.
> - The one-line description must be a factual summary of what
>   the code does, not marketing copy.

Raw draft saved verbatim as **`README.raw.md`**.

**Invented features deleted, each verified absent by grep before
removal:**

| # | Claim in `README.raw.md` | Check run | Hits | Reality |
|---|---|---|---|---|
| 1 | "embedded H2 database", "Spring Data JPA" | `grep -rinE "H2\|jpa\|Repository" src test Makefile` | **0** | storage is a `ConcurrentHashMap` in `OrderService` |
| 2 | "JWT bearer authentication and role-based access control" | `grep -rinE "jwt\|Authorization\|Bearer"` | **0** | no auth of any kind |
| 3 | "`PATCH /orders/{id}` for partial updates", "Full CRUD" | `grep -rinE "PatchMapping\|PATCH"` | **0** | only GET, GET/{id}, POST, DELETE/{id} — there is no update endpoint at all |
| 4 | "Pagination and filtering via `?page`, `?size`, `?status`" | `grep -rinE "page\|size=\|status="` | **0** | `listOrders()` takes no parameters |
| 5 | "Configurable GST rates through `application.yml`" | `grep -rinE "application.yml\|application.properties"` | **0** | the five rates are hard-coded literals in `computeVAT` |
| 6 | "Prometheus metrics at `/actuator/metrics`" | `grep -rinE "actuator\|Prometheus\|metrics"` | **0** | — |
| 7 | "Docker support via `docker compose up`" | `grep -rinE "docker"` | **0** | no Dockerfile, no compose file |
| 8 | "`make lint` — Checkstyle + SpotBugs" | `grep -E '^[a-z-]+:' Makefile` | n/a | targets are exactly `deps build test coverage mutation clean` — no `lint` |
| 9 | versioned `/api/v1` namespace in the curl example | `grep -n RequestMapping src/OrderApi.java` | n/a | the mapping is `/orders`, not `/api/v1/orders` |
| 10 | "production-ready Spring Boot microservice" on `localhost:8080` | `grep -rin springframework src` | **0** | Spring is not on the classpath; the annotations are local stubs and there is no `main` |
| 11 | "See `LICENSE` for the full text" | `ls LICENSE` | missing | no `LICENSE` file is committed |

**11 invented claims, 0 of them with any evidence in the code.**

Hand edits made on top of the deletions:
- Rewrote the one-line description myself: *"A small Java 17 teaching
  repository containing a simplified Indian income-tax/GST calculator
  and a four-endpoint order API backed by an in-memory map, built with
  `javac` and a Makefile (no build framework)."*
- Replaced the `TODO` under **Contributing** with real content —
  run `make test`, hold coverage, and check `make mutation` rather than
  coverage alone.
- Added an endpoint table built by reading `OrderApi.java`, with the
  actual status codes.
- Added an explicit sentence saying Spring is *not* on the classpath,
  so nobody reads the `@RestController` and expects a deployable
  service.
- Kept MIT as a placeholder, but said in the text that no `LICENSE`
  file exists yet instead of pointing at a file that isn't there.
- Preserved the starter README's module notes as a trailing section so
  overwriting `README.md` didn't destroy the exercise instructions.

Edited version saved as **`README.md`**.

## Part C — OpenAPI spec

**Prompt given to AI:** (handout prompt, with the full `OrderApi.java`
pasted)
> Read the following Spring @RestController and generate an
> OpenAPI 3.0 YAML spec covering:
> - Every endpoint (path, method, summary from JavaDoc).
> - Request body schemas for POST/PUT.
> - Response schemas for 2xx and 4xx.
> - Referenced DTO schemas in the components section.
> If any endpoint's behaviour is unclear, add a TODO comment
> in the spec at that location.

Output saved as **`openapi.yaml`**.

The AI used the `TODO` escape hatch once, correctly — on the error
body shape:

```yaml
    ApiError:
      type: object
      description: >-
        Shape of an error response. TODO - the controller throws
        ResponseStatusException(status, reason) but no exception handler
        is present in this repository, so the serialised body is decided
        by the framework and cannot be confirmed from the code.
```

That is exactly right: there is no `@ControllerAdvice` anywhere, so the
JSON an error actually produces is genuinely not derivable from this
source tree.

**Verification.** Rather than eyeballing it, ran the handout's three
checks mechanically:

```
GET     /orders         ['200', '500']  2xx=True 4xx=False   <-- FAIL
POST    /orders         ['201', '400']  2xx=True 4xx=True
GET     /orders/{id}    ['200', '404']  2xx=True 4xx=True
DELETE  /orders/{id}    ['204', '409']  2xx=True 4xx=True

dangling refs: none
all ops have 2xx + 4xx: False
```

- **Every endpoint appears with the correct path and method** — all
  four, matching `@GetMapping` / `@GetMapping("/{id}")` /
  `@PostMapping` / `@DeleteMapping("/{id}")`. ✅
- **Every referenced DTO is defined in components** — `OrderDto`,
  `CreateOrderRequest`, `ApiError` all referenced and all defined, no
  dangling `$ref`. ✅
- **Every method has a 2xx and a 4xx** — ❌ `GET /orders` had a `500`
  but no 4xx.

**Missing 4xx added by hand**, as the handout instructs. `listOrders()`
takes no parameters and has no client-error path of its own, so
inventing a `400` would have been exactly the hallucination Part B is
about. Instead I added the framework-level `406`, which is real for any
JSON-only `@RestController` mapping, and said in the description that
it was added by hand and why:

```yaml
        '406':
          description: >-
            The request's Accept header cannot be satisfied; this endpoint
            only produces application/json. Added by hand: the controller
            body has no client-error path of its own, so this is the
            framework-level 4xx for the mapping.
```

Re-ran the checks:

```
GET     /orders         ['200', '406', '500']  2xx=True 4xx=True
POST    /orders         ['201', '400']         2xx=True 4xx=True
GET     /orders/{id}    ['200', '404']         2xx=True 4xx=True
DELETE  /orders/{id}    ['204', '409']         2xx=True 4xx=True

dangling refs: none
all ops have 2xx + 4xx: True
```

Then validated and rendered it properly:

- `openapi-spec-validator` against the OpenAPI 3.0 schema →
  **"OpenAPI 3.0.3 spec is VALID"**.
- Rendered in **Swagger UI** (swagger-ui-dist 5, served locally over
  `python3 -m http.server`) — loads with no error banner, all four
  operations listed under `default` with the right verbs and paths, and
  all three schemas (`OrderDto`, `CreateOrderRequest`, `ApiError`)
  expandable in the Schemas section.

**Result:** `openapi.yaml` is schema-valid, renders in Swagger UI, and
passes all three of the handout's checks.

## Part D — Reflect

**How many JavaDoc inconsistencies did the AI's self-check catch on the
first pass?**

**Four**, and it caught all four in one pass — the second pass came
back clean. What is worth noting is their character. One was a flat
contradiction (`PENDING` vs `NEW`). The other three were *imports from
nearby context*: the "creation timestamp" is real but lives on `Order`
and not on the `OrderDto` that is actually returned; the 409 is real
but belongs to `cancelOrder`; "strictly positive" is the constraint a
reasonable person would expect for a money field, just not the one the
`signum() < 0` guard implements. The self-check was good precisely
because it is a different question from the one that generated the
text — "does this match the body" rather than "what would this method
plausibly do".

**How many invented features did the README pass produce?**

**Eleven** (table in Part B). The README prompt carried the same
"do NOT invent" rule as the JavaDoc prompt and it failed much harder,
because the input was a file tree instead of a method body. A file tree
is almost pure prior: seeing `OrderApi.java`, `OrderService.java` and
`OrderDto.java` is enough for the model to reach for the whole Spring
Boot starter-kit stereotype — H2, JPA, JWT, Actuator, Docker — none of
which is contradicted by a file listing, because a file listing
contradicts nothing. Several of the inventions were also *internally
coherent* (`/api/v1` in the curl example matched the `/api/v1` claimed
in the description), which makes them read as considered rather than
guessed.

**Which docs artefact needed the most hand editing, and why?**

**The README, by a wide margin** — I deleted 11 claims and an entire
Features section, then rewrote the description and Contributing by
hand; very little of `README.raw.md` survives into `README.md`.

The ranking follows exactly how tightly each artefact is pinned down by
the input:

- **OpenAPI — least editing.** One hand-added response. The controller
  *is* the spec: four annotations give four path/method pairs, the
  record components give the schemas, the thrown status codes give the
  responses. There is almost no room to invent, and where there was
  genuine uncertainty the model used the `TODO` hatch instead of
  guessing.
- **JavaDoc — middling.** The method body constrains the contract but
  does not state it, so the model filled the gap from neighbouring code
  and from convention. Four errors, all mechanically findable by the
  self-check.
- **README — most.** Nothing in a file tree constrains a claim about
  authentication or Docker, so every such claim is free to be wrong,
  and the self-check that worked for JavaDoc has nothing to check
  against.

The practical lesson is that the "do not invent" instruction did almost
no work on its own — what actually caught the hallucinations was giving
the model something concrete to be checked *against*: the method body
for the JavaDoc, and `grep` over the source for the README. Where there
was no such anchor, the rule was ignored eleven times in a single
draft.

---

# Deliverables checklist

| Item | File | Status |
|---|---|---|
| Session 5A + 5B log | `PROMPTS.md` | this file |
| Generated + audited tests | `test/TaxCalculatorTest.java` | 17 tests, all green |
| JavaDoc on chosen endpoint | `src/OrderApi.java` (`createOrder`) | verified clean on 2nd pass |
| Raw AI README draft | `README.raw.md` | unedited |
| Edited README | `README.md` | 11 invented claims removed |
| OpenAPI spec | `openapi.yaml` | schema-valid, renders in Swagger UI |
| Reconstructed controller (missing from starter) | `src/OrderApi.java`, `src/SpringStubs.java`, `src/ResponseStatusException.java`, `src/CreateOrderRequest.java` | compiles with plain `javac` |

# Final verification run

```
$ make test
[        17 tests found           ]
[        17 tests successful      ]
[         0 tests failed          ]

$ make coverage
  TOTAL LINE:   28/41 = 68.3%    (baseline 48.8%)
  TOTAL BRANCH: 26/44 = 59.1%    (baseline 34.1%)

$ make mutation
>> Line Coverage (for mutated classes only): 28/41 (68%)
>> Generated 42 mutations Killed 27 (64%)     (baseline 15/42 = 36%)
>> Mutations with no coverage 6. Test strength 75%
```

Remaining surviving mutants (all outside the chosen method, left for
honesty rather than quietly suppressed): four `changed conditional
boundary` on `computeIncomeTax`'s slab comparisons, two `negated
conditional` on `computeVAT`'s rate guard plus one boundary on its
amount guard, and two boundary mutants on `applyExemption`'s cap and
floor. `roundToPaise`
remains entirely uncovered — the handout scopes Part B to a single
method, so it was left alone deliberately.
