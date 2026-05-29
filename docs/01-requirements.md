# CSEN275 Computerized Garden System — Requirements Analysis

## 1. Problem Statement

Home and community gardens require ongoing care: watering, temperature control, pest management, and health monitoring. Manual management is costly, error-prone, and slow to respond to extreme weather or pest outbreaks.

This system is an **automated garden simulation platform** implemented in Java with an object-oriented garden model. Multiple independent subsystems (watering, climate, pest control, etc.) work together so a large, heterogeneous plant population can **survive autonomously** in a simulated environment. The system provides a **JavaFX graphical interface** for human gardeners and a **`GardenSimulationAPI`** for headless 24-hour endurance testing by scripts.

---

## 2. Stakeholders and Roles

| Role | Description |
|------|-------------|
| Home Gardener (GUI user) | Views garden state, intervenes manually, adds plants via JavaFX |
| Automation Script (API caller) | Invokes `GardenSimulationAPI` to simulate rain, temperature, and parasites |
| TA / Grader | Runs the program for ≥24h, reads `log.txt`, checks for crashes and survival |
| Development Team (3 people) | Analysis, design, implementation, documentation |

---

## 3. Feature List

### 3.1 Core Garden
- F1 Multi-type plant model (flowers, vegetables, shrubs, trees), each with water needs, parasite susceptibility, health, and growth stage
- F2 Large garden layout (grid/plots), initially ≥10 living plants covering all defined varieties
- F3 Insect/parasite entities and interactions with plants
- F4 Physical entities: sensors (soil moisture, temperature), sprinklers

### 3.2 Standalone Subsystem Modules (≥3, beyond plants)
- F5 **WateringSystem**: Auto-watering from sensors and plant needs; responds to `rain(int)`
- F6 **ClimateSystem**: Responds to `temperature(int)`; affects plant stress and recovery
- F7 **PestControlSystem**: Responds to `parasite(String)`; deploys control without instant full heal
- F8 **SimulationEngine**: 1 simulated hour = 1 simulated day; drives subsystem ticks
- F9 **EventBus**: Decoupled communication between subsystems

### 3.3 Logging and Observability
- F10 Structured `log.txt` (relative path), format: `DAY, EVENT, EVENT_VALUE, PLANTS_ALIVE`
- F11 `getState()` outputs living/dead plants and key metrics
- F12 In-GUI log viewer plus standalone log documentation

### 3.4 External API (headless testing)
- F13 `GardenSimulationAPI.initializeGarden()` — load from JSON config
- F14 `GardenSimulationAPI.getPlants()` — plant names, water requirements, parasite lists (dead plants excluded)
- F15 `rain(int)` / `temperature(int)` / `parasite(String)` / `getState()`

### 3.5 JavaFX User Interface
- F16 Garden visualization (grid, plant icons, status colors)
- F17 Manually add plants and configure water/parasite properties
- F18 Subsystem status panels (watering, climate, pest control)
- F19 Simulation controls (start/pause/speed) and Help manual entry

### 3.6 Headless Endurance
- F20 Global exception handling; single failures must not terminate the process
- F21 24h+ continuous run without crash; realistic plant state changes (no hardcoded immortality)

---

## 4. User Stories

| ID | As a… | I want to… | So that… |
|----|--------|------------|----------|
| US-01 | GUI gardener | See multiple configured plants at startup | I can quickly understand initial garden state |
| US-02 | GUI gardener | Manually add plants and set water needs and parasites | I can customize the garden layout |
| US-03 | GUI gardener | See whether watering/climate/pest systems are active | I can confirm automation is working |
| US-04 | GUI gardener | Read clear event logs in the UI | I can trace why plants died |
| US-05 | Test script | Trigger random environmental events each “hour” after `initializeGarden()` | I can simulate 24 days of conditions |
| US-06 | Test script | Call `getPlants()` for a dynamic plant list | I can verify dead plants are removed |
| US-07 | Test script | Call `getState()` after 24 hours | I can evaluate system survival |
| US-08 | TA | Read `log.txt` without verbal explanation | Grading is objective |

---

## 5. Use Case Scenarios

### Scenario A: API initialization and first day
1. Script calls `initializeGarden()`, reading `config/garden_config.json`
2. System creates ≥10 plants covering all defined types; simulation clock starts (Day 0)
3. Log entry: `0, INIT, config_loaded, 12`

### Scenario B: Rainy day
1. Script calls `rain(15)` (magnitude aligned with `waterRequirement` values)
2. `WateringSystem` increases soil moisture per plot; plant water satisfaction rises
3. If no second rain that day, moisture returns to baseline after the next day tick
4. Log: `3, RAIN, 15, 11`

### Scenario C: Hot day
1. Script calls `temperature(105)` (40–120°F)
2. `ClimateSystem` marks high heat; some plants enter stress and health declines gradually
3. Temperature resets to default at end of day
4. Log: `5, TEMPERATURE, 105, 10`

### Scenario D: Parasite outbreak
1. Script calls `parasite("aphid")`
2. `PestControlSystem` identifies vulnerable plants, applies damage; deploys bio/chemical control (no instant heal)
3. Vulnerable plants partially recover over time (parameterized)
4. Log: `7, PARASITE, aphid, 8`

### Scenario E: Plant death and list update
1. A Rose’s health drops to ≤ 0
2. Removed from active set; `getPlants()` no longer includes it
3. Log: `9, PLANT_DEATH, Rose@plot(2,3), 7`

### Scenario F: GUI gardener intervention
1. User clicks “Manual Water” or “Enable Heating”
2. Corresponding subsystem runs; event written to the same `log.txt`
3. UI refreshes plant colors/icons

---

## 6. Functional Requirements

### FR-1 Plants and Garden
- **FR-1.1** The system SHALL support at least 10 distinct plant types by `name`
- **FR-1.2** `initializeGarden()` SHALL ensure at least 10 living plants with at least one of each type
- **FR-1.3** Each plant SHALL maintain: `health` (0–100), `waterLevel`, `growthStage`, `isAlive`
- **FR-1.4** Plants SHALL define `waterRequirement` and `parasites` per type

### FR-2 Environment API
- **FR-2.1** `rain(amount)` SHALL increase moisture for the day; if not a consecutive rainy day, reset on the next simulated day
- **FR-2.2** `temperature(f)` SHALL accept 40–120; reset at end of day
- **FR-2.3** `parasite(name)` SHALL affect only plants whose `getPlants().parasites` lists include that name
- **FR-2.4** Pest control SHALL NOT instantly restore plants to full health

### FR-3 Subsystems
- **FR-3.1** Watering subsystem SHALL activate sprinklers when moisture is below threshold
- **FR-3.2** Climate subsystem SHALL trigger heating/ventilation logic at extreme temperatures
- **FR-3.3** Pest subsystem SHALL detect infestations and execute control strategies
- **FR-3.4** Subsystems SHALL tick on a unified clock and remain decoupled

### FR-4 Logging
- **FR-4.1** All events SHALL append to relative path `log.txt`
- **FR-4.2** Each log line SHALL include: simulated day, event type, event value, current living count
- **FR-4.3** `getState()` SHALL write a readable snapshot to the log

### FR-5 GUI
- **FR-5.1** SHALL provide a JavaFX main view for garden and subsystems
- **FR-5.2** SHALL allow users to add plants and edit water/parasite properties
- **FR-5.3** SHALL provide Help/User Manual (PDF or in-app)

### FR-6 Configuration
- **FR-6.1** Config file format SHALL be:
```json
{
  "plants": [
    { "name": "Rose", "amount": 5 },
    { "name": "Tomato", "amount": 5 }
  ]
}
```
- **FR-6.2** All file paths SHALL be relative (not absolute)

---

## 7. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-1 | Reliability | Run ≥24 simulated hours without crash; continue after caught exceptions |
| NFR-2 | Maintainability | Modular packages; separate interfaces from implementations |
| NFR-3 | Testability | API layer startable independently of JavaFX |
| NFR-4 | Readability | Human-readable logs; documented log fields |
| NFR-5 | Performance | Single tick completes in milliseconds (supports accelerated simulation) |
| NFR-6 | Usability | Clear GUI layout; key actions within ≤3 clicks |
| NFR-7 | Academic integrity | Cite all external sources |

---

## 8. Constraints and Assumptions

- **Tech stack**: Java 17+, JavaFX, Maven or Gradle
- **API class name**: Must be `GardenSimulationAPI` with method signatures matching the course PDF
- **Test mode**: Grading scripts do not use the GUI; GUI and API share the same domain layer
- **Assumption**: 1 API hour = 1 simulated day; health does not lock without cause

---

## 9. Acceptance Criteria

1. ✅ `initializeGarden()` + 24 environment events + `getState()` completes without uncaught exceptions
2. ✅ `getPlants()` correctly shrinks the list after plant deaths
3. ✅ `log.txt` is understandable by a TA without explanation
4. ✅ GUI supports add plant, view status, and read help
5. ✅ 24h monitoring shows no crash; living count changes realistically (not all dead, not immortal)

---

## 10. Suggested Plants and Modules (initial config reference)

**Plants (10 types)**: Rose, Tomato, Sunflower, Basil, Lavender, Marigold, Lettuce, Pepper, Daisy, OakSapling

**Subsystem modules**: WateringSystem, ClimateSystem, PestControlSystem, SimulationEngine, EventBus, LoggingService

**Entities**: Sprinkler, SoilMoistureSensor, Thermometer, Insect, Parasite
