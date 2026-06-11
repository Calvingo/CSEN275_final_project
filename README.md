# Computerized Garden Simulation

A full Java simulation of an automated garden — built for CSEN275 (Object-Oriented Programming) at Santa Clara University. The garden manages 10 plant types across a grid, runs four independent subsystems (watering, climate, pest control, and fertilizer), and exposes a clean API that a grader or test script can drive without touching the GUI.

---

## Team

Jiewen Chen, John Giannini, Atishay Jain, Liv Morgan

---

## What we built

- **10 plant types** loaded from a JSON config, each with its own water needs, heal rate, and parasite vulnerabilities
- **4 subsystems** that subscribe to an EventBus and react every simulated day:
  - `WateringSystem` — runs sprinklers, responds to rain events, resets soil moisture
  - `ClimateSystem` — applies thermal stress for extreme temps (outside 50–95°F), resets at end of day
  - `PestControlSystem` — infests only susceptible plants, runs gradual recovery (no instant heals)
  - `FertilizerSystem` — monitors nutrient levels per plot, auto-fertilizes low plots, responds to parasite events with a recovery boost
- **`GardenSimulationAPI`** — the six-method grader entry point: `initializeGarden()`, `getPlants()`, `rain()`, `temperature()`, `parasite()`, `getState()`
- **JavaFX GUI** — live grid view, subsystem status panel, add-plant dialog, manual override buttons, speed slider, and an in-app log viewer
- **84 tests**, all passing — unit, integration, endurance, and 24-day stress tests

---

## Prerequisites

- Java 17+
- Maven 3.8+

```bash
java -version
mvn -version
```

---

## Running the project

**GUI (JavaFX):**
```bash
mvn javafx:run
```

**Headless API test (no GUI):**
```bash
mvn exec:java -Dexec.mainClass=com.csen275.garden.app.HeadlessSimulationRunner
```

**Or use the shell script:**
```bash
bash script/run-24h-test.sh
```

**All tests:**
```bash
mvn test
```

All file paths are relative, so run commands from the project root.

---

## Project layout

```
CSEN275_final_project/
├── config/
│   ├── garden_config.json       # which plants to plant and how many
│   └── plant_definitions.json   # water needs, heal rates, parasite vulnerabilities
├── docs/
│   ├── 01-requirements.md
│   ├── 02-uml-design.md         # use case, class, object, activity, sequence diagrams
│   ├── log-guide.md             # log.txt format reference
│   └── user-manual.md           # in-app Help button reads this
├── script/
│   └── run-24h-test.sh
├── src/main/java/com/csen275/garden/
│   ├── api/          # GardenSimulationAPI — grader entry point
│   ├── app/          # GardenApp, GardenLauncher, HeadlessSimulationRunner
│   ├── config/       # JSON config loader and POJOs
│   ├── domain/       # Garden, GardenGrid, Plot, PlantInstance, PlantType, sensors, insects
│   ├── event/        # EventBus, GardenEvent, EventType
│   ├── logging/      # LoggingService (writes log.txt)
│   ├── module/       # WateringSystem, ClimateSystem, PestControlSystem, FertilizerSystem
│   ├── simulation/   # SimulationEngine, SimulationClock, EnvironmentEventGenerator
│   └── ui/           # MainController, AddPlantDialog, GardenUiSession
└── src/test/java/    # 17 test classes, 84 tests
```

---

## How it works

Each simulated day runs through one call to `tickHour()`:

1. `EventBus` notifies all four modules of day start
2. Every plot ticks (soil moisture drops, nutrients deplete, plants take or recover health)
3. Dead plants are removed and logged as `PLANT_DEATH`
4. `EventBus` notifies all four modules of day end — sprinklers fire, thermal stress applies, pests tick, low-nutrient plots get fertilized
5. The clock advances

Rain, temperature, and parasite events arrive before the tick (via the API or GUI) and are published to the bus immediately so the right module handles them.

---

## Docs

- **UML diagrams** (use case, class, object, activity, sequence): [`docs/02-uml-design.md`](docs/02-uml-design.md)
- **Log format**: [`docs/log-guide.md`](docs/log-guide.md)
- **User manual**: [`docs/user-manual.md`](docs/user-manual.md) (also accessible from the Help button in the app)
