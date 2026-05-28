# Intelligent Bus Driver Guidance System

ISYS3413 / ISYS3475 / ISYS1118 - Software Engineering Fundamentals
Assignment 4 (Team-based) - Group **G17**

A Java + Maven project that manages Drivers and Buses, validates the
business rules from the assignment brief, and persists data to
human-readable TXT files.

## Build & test

```bash
mvn -B clean test
```

## Rules enforced

**Driver** (`DriverValidator`)

- D1 - driverID: 10 chars, first 2 digits 2-9, ≥2 specials in chars 3-8, last 2 upper A-Z; unique.
- D2 - address: `Street Number|Street Name|City|State|Country`.
- D3 - birthdate: `DD-MM-YYYY`, not in the future.
- D4 - license cannot change once experience > 10y.
- D5 - driverID and name are immutable on update.

**Bus** (`BusValidator`)

- B1 - busID: exactly 8 digit chars; unique.
- B2 - capacity cannot increase on update.
- B3 - drivers older than 50 cannot operate capacity ≥ 50.
- B4 - electric buses require ≥ 5 years experience.
- B5 - electric/hybrid require Heavy or PublicTransport license.

## Project layout

```
BusDriverGuidanceSystem/
  pom.xml
  data/             drivers.txt, buses.txt (TXT 'database')
  src/main/java/com/busdriver/
    exception/      ValidationException
    model/          Driver, Bus
    validator/      DriverValidator, BusValidator
    repository/     DriverRepository, BusRepository
  src/test/java/com/busdriver/
    unit/           DriverUnitTest, BusUnitTest
    integration/    DriverIntegrationTest, BusIntegrationTest
```

## Authors (G17)

Lee Sunjin (s3911760), Max Busuttil (s4169373),
Ethan Patten-Cox (s4167742), Martin Tippett (s4168666).
