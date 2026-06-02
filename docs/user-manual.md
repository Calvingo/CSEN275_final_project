# Garden Simulation — User Manual

## Prerequisites

- Java 17 or newer
- Maven 3.8+

Verify:

```bash
java -version
mvn -version
```

## Build and run the GUI

From the project root:

```bash
mvn javafx:run
```

If your IDE shows `JavaFX runtime components are missing`, run through Maven or use main class `com.csen275.garden.app.GardenLauncher` (not `GardenApp` directly).

The JavaFX window opens with the garden grid, subsystem status, toolbar, and log viewer.

## Build and run headless API test

For grading-style endurance testing without the GUI:

```bash
mvn test
bash script/run-24h-test.sh
```

To keep the JVM alive for real wall-clock monitoring (optional overnight run):

```bash
mvn -q exec:java -Dexec.mainClass=com.csen275.garden.app.HeadlessSimulationRunner \
  -Dgarden.keepAlive=true -Dgarden.keepAliveHours=24
```

## GUI walkthrough

### Toolbar

| Control | Action |
|---------|--------|
| **Initialize** | Loads `config/garden_config.json` and `config/plant_definitions.json`, starts Day 0, clears and recreates `log.txt` |
| **Start / Pause** | Automatically advances one simulated day every ~2 seconds with a **random** rain, heat wave (40–120°F), or parasite outbreak, then ticks the garden |
| **Manual Water** | Runs the watering subsystem sprinklers on dry plots and logs `MANUAL_WATER` |
| **Apply Fertilizer** | Applies nutrients to all planted plots and logs `MANUAL_FERTILIZER` |
| **Add Plant** | Opens a dialog to place a custom plant on the next open grid cell |
| **Rain (15)** | Simulates rain then advances one day (same as API `rain` + tick) |
| **Heat (105°F)** | Simulates high temperature then advances one day |
| **Parasite (aphid)** | Triggers an aphid outbreak then advances one day |
| **Log State** | Writes a `STATE` snapshot to `log.txt` |
| **Help** | Opens this manual |

### Garden grid (center)

- **Green** — plant health ≥ 60
- **Yellow** — health 30–59
- **Red** — health below 30
- **Gray** — empty plot or dead plant removed from grid

Hover a cell for health, water level, soil moisture, and growth stage.

### Subsystem panel (right)

- **Watering** — whether rain was absorbed today
- **Climate** — current simulated temperature (°F), heating/cooling status
- **Pest control** — active parasite treatments or proactive scans
- **Fertilizer** — plots treated today or nutrient monitoring status

### Log panel (bottom)

Shows the last 50 lines of `log.txt`. Format:

```
DAY, EVENT, EVENT_VALUE, PLANTS_ALIVE
```

See `docs/log-guide.md` for every event type.

## Adding plants manually

1. Click **Initialize** if the garden is not loaded.
2. Click **Add Plant**.
3. Enter name, water requirement, and comma-separated parasites (e.g. `aphid, spider_mite`).
4. Click **OK**. The plant appears on the next free grid cell.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `mvn javafx:run` fails with JavaFX errors | Use Java 17+; ensure `pom.xml` JavaFX plugin `mainClass` is `com.csen275.garden.app.GardenLauncher` |
| Grid stays empty | Click **Initialize** first |
| `log.txt` empty | Initialize or trigger any simulation action |
| No empty plots for new plants | Grid is 5×5; remove dead plants by letting simulation run or restart with Initialize |

## Configuration files

- `config/garden_config.json` — plant counts at startup
- `config/plant_definitions.json` — water needs, heal rates, parasite lists

All paths are relative to the project root.
