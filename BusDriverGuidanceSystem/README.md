# Intelligent Bus Driver Guidance System

> ISYS3413 / ISYS3475 / ISYS1118 - Software Engineering Fundamentals
> Assignment 4 (Team-based) - Group **G17**

Iteration **0.4.0** of the Intelligent Bus Driver Guidance System
(progress ~40%). Validators for all Driver (D1-D5) and Bus (B1-B5)
business rules are now in place; persistence and tests will follow
in subsequent commits.

## What is included in this iteration (~40%)

- `pom.xml` (Java 17, no test dependencies yet)
- `src/main/java/com/busdriver/`
  - `exception/ValidationException.java`
  - `model/Driver.java`  (with `getAge()` helper for B3)
  - `model/Bus.java`     (with optional `assignedDriverID` for B3-B5)
  - `validator/DriverValidator.java`  (full D1-D5)
  - `validator/BusValidator.java`     (full B1-B5 incl. assignment)
  - `repository/DriverRepository.java`  (in-memory, validator-backed)
  - `repository/BusRepository.java`     (in-memory, validator-backed,
                                         `assignDriver` operation)
- `data/drivers.txt`, `data/buses.txt` (placeholders - I/O later)
- `.gitignore`

## What is NOT here yet (planned for later commits)

- Persistence to drivers.txt / buses.txt on every mutation.
- A reloadable repository constructor that loads the file on startup.
- JUnit 5 unit and integration tests.
- A GitHub Actions workflow that runs the test suite on every push.
- Demo video script.

## Build

```bash
mvn -B clean compile
```

A `mvn test` command will be available once the test dependencies and
the first batch of unit tests are added in a subsequent commit.

## Rules currently enforced

### Driver (DriverValidator)
| Rule | Description |
|------|-------------|
| D1   | `driverID` exactly 10 chars: first 2 digits 2-9, >=2 specials in chars 3-8, last 2 upper A-Z; unique. |
| D2   | `address` = `Street Number\|Street Name\|City\|State\|Country`. |
| D3   | `birthdate` DD-MM-YYYY, not in the future. |
| D4   | License cannot change once experience > 10y. |
| D5   | `driverID` and `name` cannot be modified on update. |

### Bus (BusValidator)
| Rule | Description |
|------|-------------|
| B1   | `busID` exactly 8 digit chars; unique. |
| B2   | `capacity` cannot increase on update. |
| B3   | Drivers older than 50 cannot operate capacity >= 50. |
| B4   | Electric buses require >= 5 years experience. |
| B5   | Electric/Hybrid require Heavy or PublicTransport license. |
