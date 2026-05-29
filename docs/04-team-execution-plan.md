# Execution Plan — Build Order

Follow these steps **in order**. Do not skip ahead until the tests for the current step pass.

**Repo:** https://github.com/Calvingo/CSEN275_final_project  
**Stack:** Java 17+, Maven, JavaFX 21, JUnit 5  
**All code, comments, and docs must be in English.**

Reference documents: `docs/01-requirements.md`, `docs/02-uml-design.md`, `docs/Gardening System APIs.pdf`

---

## Git workflow

```bash
git pull origin main
git checkout -b step-XX-short-name
# complete the step
git add .
git commit -m "Step XX: short description"
git push -u origin step-XX-short-name
# open a Pull Request → merge to main
```

Merge only when that step's tests pass.

---

## Step 0 — Project scaffold

**Depends on:** nothing

### Work

- Create Maven project: Java 17, package base `com.csen275.garden`
- Add dependencies: JavaFX 21, JUnit 5, Jackson
- Create package structure per `docs/02-uml-design.md` section 9
- Add `config/garden_config.json` and `config/plant_definitions.json` with 10 plant types:
  Rose, Tomato, Sunflower, Basil, Lavender, Marigold, Lettuce, Pepper, Daisy, OakSapling
- Add `README.md` with prerequisites and build/run commands
- Extend `.gitignore` for `target/`, `log.txt`, IDE files
- Use relative paths only; no business logic yet

### Tests

```bash
git clone https://github.com/Calvingo/CSEN275_final_project.git
cd CSEN275_final_project
git pull origin main
java -version    # must show 17+
mvn -q test      # BUILD SUCCESS
ls config/garden_config.json config/plant_definitions.json
```

**Pass criteria:** Maven builds; config files exist; package folders exist under `src/main/java/com/csen275/garden/`.

---

## Step 1 — Plant domain model

**Depends on:** Step 0

### Work

Create in `com.csen275.garden.domain.plant`:

- `GrowthStage` enum: SEEDLING, GROWING, MATURE, STRESSED, RECOVERING, DYING, DEAD
- `PlantType`: name, waterRequirement, parasites, healRate, `createInstance()`
- `PlantInstance`: id, type, health (0–100), waterLevel, stage, isAlive
  - Methods: `applyStress(int)`, `applyWater(int)`, `tickNaturalRecovery()`, `tickDaily()`
- Rule: health ≤ 0 → `isAlive = false`, stage = DEAD

Add JUnit tests: death at zero health, water increases waterLevel, natural recovery over ticks.

### Tests

```bash
mvn test -Dtest=Plant*
```

**Pass criteria:**
- [ ] `PlantInstance` with health 0 → `isAlive()` false
- [ ] All plant unit tests pass

---

## Step 2 — Garden grid and plot

**Depends on:** Step 1

### Work

Create in `com.csen275.garden.domain.garden`:

- `Plot`: soilMoisture, optional PlantInstance, `applyWater(int)`, `tickDay()`
- `GardenGrid`: rows × cols (at least 4×4), `placePlant()`, `getPlot(x,y)`, `getAllPlots()`
- `Garden`: grid + living plant list; `getLivingCount()`, `getLivingPlants()`, `removeDead()`, `placePlantOnGrid()`

Create in `com.csen275.garden.domain.sensor`:

- `Sensor` interface, `SoilMoistureSensor`, `Sprinkler`, `Thermometer`

`removeDead()` must remove dead plants from the living list and clear plot references.

Add JUnit tests: place plant, remove dead, soil moisture changes on water.

### Tests

```bash
mvn test
```

**Pass criteria:**
- [ ] Can place 12 plants on grid
- [ ] Dead plant removed from `getLivingPlants()` after `removeDead()`

---

## Step 3 — Config loader

**Depends on:** Step 0, Step 2

### Work

Create in `com.csen275.garden.config`:

- `GardenConfig` matching API format: `{ "plants": [ { "name", "amount" } ] }`
- `PlantDefinitionConfig` for `config/plant_definitions.json`
- `ConfigLoader`: `loadGardenConfig()`, `loadPlantDefinitions()` via Jackson, relative paths

Add `Garden.loadFromConfig(...)`:

- Creates `PlantType` from definitions
- Spawns configured amounts; ≥ 10 living plants; every type has ≥ 1 instance

Add JUnit tests: both JSON files load; loadFromConfig meets count and variety requirements.

### Tests

```bash
mvn test
cat config/garden_config.json
cat config/plant_definitions.json
```

**Pass criteria:**
- [ ] Both JSON files load
- [ ] After `loadFromConfig`, living count ≥ 10 and all varieties present

---

## Step 4 — Logging service

**Depends on:** Step 0

### Work

Create `com.csen275.garden.logging.LoggingService`:

- Append to relative path `log.txt`
- `log(int day, String event, String eventValue, int plantsAlive)` — format: `DAY, EVENT, EVENT_VALUE, PLANTS_ALIVE`
- `logState(Garden garden)` — STATE event with alive/dead summary
- `clearLog()` for tests; thread-safe append; no `System.out` for production events

Create `docs/log-guide.md` listing EVENT types (INIT, RAIN, TEMPERATURE, PARASITE, PLANT_DEATH, STATE, ERROR, etc.)

Add JUnit tests: correct line format, append without overwrite, logState writes STATE.

### Tests

```bash
mvn test
head -5 log.txt
```

**Pass criteria:**
- [ ] Line format matches: `0, INIT, test, 10`
- [ ] `docs/log-guide.md` exists

---

## Step 5 — Event bus and GardenModule interface

**Depends on:** Steps 2, 4

### Work

Create:

- `com.csen275.garden.event.EventType`: RAIN, TEMPERATURE, PARASITE, DAY_START, DAY_END, MANUAL_WATER, etc.
- `com.csen275.garden.event.GardenEvent`: type, day, payload, intValue
- `com.csen275.garden.module.GardenModule`: `onDayStart`, `onDayEnd`, `onEvent`, `getName()`
- `com.csen275.garden.simulation.EventBus`: subscribe, publish

Handlers wrapped in try/catch; one module failure must not break others. Log ERROR on handler failure.

Add JUnit tests: event delivery; exception isolation.

### Tests

```bash
mvn test
```

**Pass criteria:**
- [ ] EventBus delivers events to all subscribers
- [ ] Exception in one module does not crash bus

---

## Step 6 — WateringSystem module

**Depends on:** Steps 2, 4, 5

### Work

Create `com.csen275.garden.module.WateringSystem` implementing `GardenModule`:

- `handleRain(int)`: increase plot soilMoisture and plant waterLevel
- `activateSprinklers()`: water dry plots when moisture below threshold
- `resetDailyMoisture()`: on DAY_END, reset rain boost unless still rainy
- Respond to RAIN events; log RAIN and SPRINKLER events

Per API spec: rain effect resets after 1 simulated day if that day is not also rainy.

Add JUnit tests: rain increases moisture; sprinklers on dry soil; daily reset.

### Tests

```bash
mvn test -Dtest=Watering*
```

**Pass criteria:**
- [ ] Rain increases soil moisture
- [ ] `resetDailyMoisture()` restores baseline after non-rainy day
- [ ] Logs contain RAIN events

---

## Step 7 — ClimateSystem module

**Depends on:** Steps 2, 4, 5

### Work

Create `com.csen275.garden.module.ClimateSystem` implementing `GardenModule`:

- `currentTempF`, `defaultTempF` (e.g. 72)
- `setTemperature(int)`: accept 40–120; log invalid values as ERROR without crashing
- `resetDaily()`: restore default on DAY_END
- `applyThermalStress(Garden)`: gradual stress for hot (>95°F) or cold (<50°F)

Per API spec: temperature sets during the day, resets after day ends.

Add JUnit tests: stress at 105°F; daily reset; invalid temp handled safely.

### Tests

```bash
mvn test -Dtest=Climate*
```

**Pass criteria:**
- [ ] Valid temps 40–120 accepted
- [ ] End of day resets temperature
- [ ] Extreme temp reduces health gradually, not instantly to 0

---

## Step 8 — PestControlSystem module

**Depends on:** Steps 1, 4, 5

### Work

Create `com.csen275.garden.module.PestControlSystem` implementing `GardenModule`:

- `triggerParasite(String)`: damage plants whose type lists that parasite
- `deployControl()`: reduce infestation intensity; do NOT set health to 100
- `tickInfestations()`: ongoing damage + gradual recovery via healRate
- Log PARASITE and PEST_CONTROL events

Per API spec: pest control must not instantly heal to full health.

Add JUnit tests: vulnerable vs immune plants; control does not max health; recovery over multiple ticks.

### Tests

```bash
mvn test -Dtest=Pest*
```

**Pass criteria:**
- [ ] Only vulnerable plants damaged
- [ ] After control deploy, health < 100 unless already 100
- [ ] Recovery happens over multiple ticks

---

## Step 9 — SimulationEngine

**Depends on:** Steps 5, 6, 7, 8

### Work

Create in `com.csen275.garden.simulation`:

- `SimulationClock`: currentDay (starts 0), `incrementDay()`
- `SimulationEngine`:
  - Constructor(Garden, LoggingService, List<GardenModule>)
  - `start()`: day 0 begins, log INIT
  - `tickHour()`: one simulated day — onDayStart → env event → module logic → plant tickDaily → removeDead → onDayEnd → log
  - `onRain`, `onTemperature`, `onParasite`: apply env events via EventBus
  - All ticks wrapped in try/catch

Register WateringSystem, ClimateSystem, PestControlSystem by default.

Add `SimulationEngineTest`: day advances; rain/parasite effects visible in log.

### Tests

```bash
mvn test -Dtest=SimulationEngine*
```

**Pass criteria:**
- [ ] `tickHour()` advances day
- [ ] Dead plants removed each tick
- [ ] Engine survives injected exception in one module

---

## Step 10 — Domain integration test

**Depends on:** Steps 3, 4, 9

### Work

Create `GardenIntegrationTest`:

1. Load config via ConfigLoader
2. Build Garden with plant definitions
3. Create LoggingService, three modules, SimulationEngine
4. `engine.start()`
5. Simulate 5 days: mix of rain(10), temperature(90), parasite("aphid")
6. Assert: living count > 0; log has INIT + RAIN/TEMPERATURE/PARASITE; no uncaught exceptions

### Tests

```bash
mvn test -Dtest=GardenIntegrationTest
cat log.txt | head -20
```

**Pass criteria:**
- [ ] After 5 days, at least 1 plant alive
- [ ] log.txt readable and formatted correctly
- [ ] Full `mvn test` passes

---

## Step 11 — GardenSimulationAPI

**Depends on:** Steps 3, 4, 9

### Work

Create `com.csen275.garden.api.GardenSimulationAPI` per `docs/Gardening System APIs.pdf`:

- `void initializeGarden()` — load configs, build garden, start clock; ≥ 10 plants, all varieties
- `Map<String, Object> getPlants()` — keys: `plants`, `waterRequirement`, `parasites`; dead plants excluded
- `void rain(int amount)`
- `void temperature(int fahrenheit)`
- `void parasite(String name)`
- `void getState()` — log STATE to log.txt

Standalone (no JavaFX). Each env call should advance simulation by one day (1 hour = 1 day). Relative paths only.

Add `GardenSimulationAPITest`: init + getPlants; dead plant shrinks list; 24-iteration loop without exception.

### Tests

```bash
mvn test -Dtest=GardenSimulationAPITest
```

**Pass criteria:**
- [ ] All 6 public methods with correct signatures
- [ ] `getPlants()` returns all three keys, size ≥ 10
- [ ] 24-iteration test completes without JavaFX

---

## Step 12 — 24-hour headless runner

**Depends on:** Step 11

### Work

Create `com.csen275.garden.app.HeadlessSimulationRunner` with `main()`:

1. `new GardenSimulationAPI()`
2. `initializeGarden()`
3. Random loop hours 1–24: rain, temperature(40–120), or parasite
4. `getState()`

Create `script/run-24h-test.sh`:

```bash
mvn -q exec:java -Dexec.mainClass=com.csen275.garden.app.HeadlessSimulationRunner
```

Add `HeadlessSimulationRunnerTest` (24 iterations in-process). Add exec plugin to pom.xml if missing. Document in README.

### Tests

```bash
mvn test
bash script/run-24h-test.sh
tail -20 log.txt
```

**Pass criteria:**
- [ ] Runner completes 24 iterations without crash
- [ ] Final getState() in log
- [ ] PLANTS_ALIVE > 0 at end (with realistic balance)

---

## Step 13 — JavaFX UI

**Depends on:** Steps 9, 11

### Work

Create:

- `com.csen275.garden.app.GardenApp` (JavaFX Application)
- `com.csen275.garden.ui.MainController`

Layout (BorderPane or similar):

- **Center:** GridPane — cells colored by plant health (green/yellow/red/gray)
- **Right:** Subsystem status (Watering, Climate, Pest)
- **Bottom:** TextArea log tail (last 50 lines from log.txt)
- **Top:** Initialize, Start/Pause, Manual Water, Help

Wire to the same SimulationEngine + Garden as the API (shared factory — no duplicated logic).

Add-plant dialog: name, water requirement, comma-separated parasites.

Update pom.xml for `mvn javafx:run`.

### Tests (manual)

```bash
mvn javafx:run
```

**Pass criteria:**
- [ ] Window opens without exception
- [ ] Grid shows plants after Initialize
- [ ] Log area updates on actions
- [ ] Add Plant dialog works
- [ ] Help opens user manual

---

## Step 14 — User manual

**Depends on:** Step 13

### Work

Create `docs/user-manual.md`:

- Build and run GUI (`mvn javafx:run`)
- Run headless API test
- GUI walkthrough
- How to add plants manually
- Subsystem panel descriptions
- Troubleshooting (Java version, JavaFX)

Update README with clone, build, test commands. Help button in JavaFX loads manual content.

### Tests

```bash
mvn javafx:run
cat docs/user-manual.md
```

**Pass criteria:**
- [ ] `docs/user-manual.md` complete
- [ ] README has build/run/test sections
- [ ] Help accessible from GUI

---

## Step 15 — Final integration

**Depends on:** Steps 1–14

### Work

Review against `docs/01-requirements.md` and API PDF:

1. ≥ 3 standalone modules (Watering, Climate, Pest)
2. Large garden: many plants, sprinklers, sensors, insects in model
3. Consistent log.txt format via LoggingService
4. No absolute file paths in codebase
5. Global exception handling — JVM survives 24h run
6. Plants are not immortal
7. Pest control does not instant-heal to 100
8. All code and comments in English

Fix gaps. Add EnduranceTest (24 loops) to `mvn test`.

### Tests

```bash
mvn clean test
bash script/run-24h-test.sh
grep -r "/Users/" src/ || echo "OK: no absolute user paths"
grep -r "C:\\\\" src/ || echo "OK: no Windows absolute paths"
grep -E "^[0-9]+, [A-Z_]+," log.txt | head -5
mvn javafx:run
mvn -q exec:java -Dexec.mainClass=com.csen275.garden.app.HeadlessSimulationRunner
```

**Pass criteria:**

| # | Requirement | Verify |
|---|-------------|--------|
| 1 | JavaFX GUI | `mvn javafx:run` works |
| 2 | ≥3 modules | Watering, Climate, Pest implement GardenModule |
| 3 | Large garden | ≥10 types, ≥10 plants at init |
| 4 | log.txt | Format `DAY, EVENT, EVENT_VALUE, PLANTS_ALIVE` |
| 5 | GardenSimulationAPI | All 6 methods; headless test passes |
| 6 | 24h survival | Runner completes without crash |
| 7 | Relative paths | No absolute paths in src |
| 8 | User manual | docs/user-manual.md + Help button |
| 9 | Realistic survival | Plants can die; system keeps running |

---

## Step 16 — Submit

**Depends on:** Step 15

### Work

```bash
git pull origin main
mvn clean test
bash script/run-24h-test.sh
git add .
git commit -m "Final integration: grading-ready build"
git push origin main
```

Optional overnight run (matches TA grading):

```bash
nohup bash script/run-24h-test.sh > endurance.out 2>&1 &
tail endurance.out
tail log.txt
```

**Pass criteria:**
- [ ] Process exited 0 or still running after 24h
- [ ] log.txt complete and readable
- [ ] GitHub main branch up to date

---

## Merge order

```
Step 0  → main
Steps 1–4  → PR → main
Steps 5–10 → PR → main  (after 1–4 merged)
Steps 11–14 → PR → main (after 9–10 merged)
Steps 15–16 → PR → main
```

Do not work on the same files on parallel branches without coordinating via Pull Requests.
