# Intelligent Bus Driver Guidance System

> ISYS3413 / ISYS3475 / ISYS1118 - Software Engineering Fundamentals
> Assignment 4 (Team-based) - Group **G17**

Initial scaffolding for the Intelligent Bus Driver Guidance System.
This first commit contains only the Maven build file and the four
domain/repository classes mentioned in the assignment brief.

## What is included in this commit (~15%)

- `pom.xml` (Java 17, no test dependencies yet)
- `src/main/java/com/busdriver/model/Driver.java`
- `src/main/java/com/busdriver/model/Bus.java`
- `src/main/java/com/busdriver/repository/DriverRepository.java`
   - in-memory `add`, `retrieve`, `retrieveAll`, `update`, `count`
- `src/main/java/com/busdriver/repository/BusRepository.java`
   - in-memory `add`, `retrieve`, `retrieveAll`, `update`, `count`
- `data/drivers.txt`, `data/buses.txt` (placeholders)
- `.gitignore`

## What is NOT here yet (planned for later commits)

- Validators that enforce Driver rules **D1-D5** and Bus rules **B1-B5**.
- TXT-file persistence in both repositories.
- A driver-bus assignment operation (B3, B4, B5).
- JUnit 5 unit and integration tests.
- A GitHub Actions workflow that runs the test suite on every push.
- Demo video script.

## Build

```bash
mvn -B clean compile
```

A `mvn test` command will be available once the test dependencies and
the first batch of unit tests are added in a subsequent commit.
