# Atishay Jain — Contribution Log
## CSEN275 Final Project: Computerized Garden Simulation

> This file documents every design decision, implementation choice, and justification
> for work contributed by Atishay Jain to the group project.
> Updated after each completed step.

---

## Step 0 — Project Scaffold
**Date:** 2026-05-29
**Branch:** `step-00-scaffold`

### What was done
- Initialized Maven project (`com.csen275.garden`, Java 21, Maven 3.9)
- Added all required dependencies: JavaFX 21, JUnit 5 (5.10.2), Jackson Databind (2.17.1)
- Created complete package directory tree matching the UML design document (`docs/02-uml-design.md`, Section 9):
  ```
  api/ app/ config/ domain/{garden,plant,sensor}/ module/
  simulation/ event/ logging/ ui/
  ```
- Created `config/garden_config.json` — 10 plant types (Rose, Tomato, Sunflower, Basil,
  Lavender, Marigold, Lettuce, Pepper, Daisy, OakSapling) with amounts totalling ≥ 12,
  satisfying FR-1.2 (at least 10 living plants, all varieties present at init)
- Created `config/plant_definitions.json` — extended type attributes: `waterRequirement`,
  `healRate`, `parasites` list per plant; aligns with `getPlants()` return contract (API spec p.2)
- Added `script/run-24h-test.sh` for headless 24-hour endurance testing (Step 12 requirement)
- Added Maven exec and javafx plugins so `mvn javafx:run` and `mvn exec:java` work without
  additional CLI flags
- Added `ScaffoldTest.java` — verifies config files and package dirs exist; `mvn test` → BUILD SUCCESS

### Design justifications
| Decision | Reason |
|----------|--------|
| Java 21 (not 17) | Installed JDK is 21; 21 is LTS and backward-compatible with all Java 17 features used |
| Jackson for JSON | Industry standard; simpler than manual parsing; ObjectMapper handles both config files |
| Separate `garden_config.json` + `plant_definitions.json` | Config file carries runtime amounts; definitions carry type biology (waterReq, parasites, healRate). Splitting avoids repeating biology data per garden instance and satisfies the API config format exactly |
| 10 plant types with ≥12 total instances | Guarantees FR-1.2 (≥10 living, all varieties) even if 2 die between init and first assertion |
| `healRate` field in definitions | Required for PestControlSystem (Step 8) — pest control must NOT instant-heal, plants recover at `healRate` HP/tick |
| Relative paths in config and script | API spec explicitly forbids absolute paths; all file I/O uses `Path.of("config/...")` |

---

## Upcoming Steps (planned)
- **Step 1** — Plant domain model (GrowthStage, PlantType, PlantInstance)
- **Step 2** — Garden grid and Plot
- **Step 3** — ConfigLoader
- **Step 4** — LoggingService + log.txt format
- **Step 5** — EventBus + GardenModule interface
- **Step 6** — WateringSystem
- **Step 7** — ClimateSystem
- **Step 8** — PestControlSystem
- **Step 9** — SimulationEngine
- **Step 10** — Domain integration test
- **Step 11** — GardenSimulationAPI
- **Step 12** — 24-hour headless runner
