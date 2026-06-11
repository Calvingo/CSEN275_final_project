# CSEN275 Computerized Garden System — UML Design

> This document includes the main UML views required for the course. Render diagrams in [PlantUML](https://plantuml.com/) or any Mermaid-compatible editor.

---

## 1. Use Case Diagram

```mermaid
flowchart LR
    subgraph Actors
        Gardener((GUI Gardener))
        Script((Test Script))
        TA((TA / Grader))
    end

    subgraph GardenSystem[Computerized Garden System]
        UC1[Initialize Garden]
        UC2[View Garden State]
        UC3[Add / Configure Plants]
        UC4[Manual Subsystem Override]
        UC5[Simulate Rain]
        UC6[Simulate Temperature]
        UC7[Simulate Parasite]
        UC8[Get Plant Definitions]
        UC9[Export State Log]
        UC10[Read log.txt]
        UC11[24h Endurance Run]
    end

    Gardener --> UC1
    Gardener --> UC2
    Gardener --> UC3
    Gardener --> UC4
    Script --> UC1
    Script --> UC5
    Script --> UC6
    Script --> UC7
    Script --> UC8
    Script --> UC9
    Script --> UC11
    TA --> UC10
    TA --> UC11
    UC4 -.-> Watering[Watering Subsystem]
    UC4 -.-> Climate[Climate Subsystem]
    UC4 -.-> Pest[Pest Control Subsystem]
    UC4 -.-> Fertilizer[Fertilizer Subsystem]
    UC5 -.-> Watering
    UC6 -.-> Climate
    UC7 -.-> Pest
```

> **Note:** Manual fertilizer (`FertilizerSystem.applyManualFertilizer`) is a GUI-only override under UC4. The headless API exposes rain, temperature, and parasite events only.

---

## 2. Component Diagram

```mermaid
flowchart TB
    subgraph Presentation
        UI[JavaFX UI / GardenUiSession]
        API[GardenSimulationAPI]
    end

    subgraph Application
        Engine[SimulationEngine]
        EventBus[EventBus]
    end

    subgraph Subsystems
        Watering[WateringSystem]
        Climate[ClimateSystem]
        Pest[PestControlSystem]
        Fertilizer[FertilizerSystem]
    end

    subgraph Infrastructure
        Logger[LoggingService]
        Config[ConfigLoader]
        Clock[SimulationClock]
    end

    subgraph Domain
        Garden[Garden / GardenGrid / Plot]
        Plants[PlantType / PlantInstance]
        Insects[Insect / Parasite]
    end

    UI --> Engine
    API --> Engine
    UI --> Config
    API --> Config

    Engine --> EventBus
    Engine --> Clock
    EventBus --> Watering
    EventBus --> Climate
    EventBus --> Pest
    EventBus --> Fertilizer

    Watering --> Garden
    Climate --> Garden
    Pest --> Garden
    Fertilizer --> Garden

    Garden --> Plants
    Pest --> Insects

    Engine --> Logger
    Watering --> Logger
    Climate --> Logger
    Pest --> Logger
    Fertilizer --> Logger
```

---

## 3. Class Diagram (Core)

```mermaid
classDiagram
    class GardenSimulationAPI {
        -SimulationEngine engine
        -Garden garden
        -LoggingService logger
        +initializeGarden()
        +getPlants() Map
        +rain(amount:int)
        +temperature(fahrenheit:int)
        +parasite(name:String)
        +getState() void
    }

    class SimulationEngine {
        -SimulationClock clock
        -EventBus eventBus
        -Garden garden
        +start()
        +tickHour()
        +onRain(amount:int)
        +onTemperature(temp:int)
        +onParasite(name:String)
        +getCurrentDay() int
    }

    class GardenModule {
        <<interface>>
        +onDayStart(day:int)
        +onDayEnd(day:int)
        +onEvent(event:GardenEvent)
        +getName() String
    }

    class WateringSystem {
        -boolean rainDuringCurrentDay
        +handleRain(day:int, amount:int)
        +activateSprinklers(day:int)
        +resetDailyMoisture()
    }

    class ClimateSystem {
        -int currentTempF
        +setTemperature(day:int, temp:int)
        +applyThermalStress(day:int)
        +resetDaily(day:int)
    }

    class PestControlSystem {
        -List~Parasite~ activeParasites
        -Set~String~ treatedPlantIds
        +triggerParasite(day:int, name:String)
        +deployControl(day:int, name:String)
        +tickInfestations(day:int)
    }

    class FertilizerSystem {
        -int plotsTreatedToday
        +fertilizeLowPlots(day:int)
        +applyManualFertilizer(day:int)
        +boostNutrientsAfterParasite(day:int)
    }

    class EventBus {
        +publish(event:GardenEvent)
        +subscribe(module:GardenModule)
        +notifyDayStart(day:int)
        +notifyDayEnd(day:int)
    }

    class LoggingService {
        -Path logPath
        +log(day:int, event:String, value:String, alive:int)
        +logState(day:int, garden:Garden)
    }

    class Garden {
        -GardenGrid grid
        -List~PlantInstance~ livingPlants
        +loadFromConfig(config, definitions)
        +tickDay() List
        +getLivingCount() int
        +getLivingPlants() List
        +removeDead() List
    }

    class GardenGrid {
        -int rows
        -int cols
        -Plot[][] plots
        +getPlot(row:int, col:int) Plot
        +placePlant(plant:PlantInstance, row:int, col:int) bool
    }

    class Plot {
        -PlantInstance plant
        -int soilMoisture
        -int nutrientLevel
        +applyWater(amount:int)
        +applyFertilizer(amount:int)
        +tickDay()
    }

    class PlantType {
        +String name
        +int waterRequirement
        +List~String~ parasites
        +int healRate
        +createInstance() PlantInstance
    }

    class PlantInstance {
        +String id
        +PlantType type
        +int health
        +int waterLevel
        +GrowthStage stage
        +boolean alive
        +applyStress(amount:int)
        +applyWater(amount:int)
        +applyRecovery(amount:int)
        +tickDaily(nutrientLevel:int)
        +tickNaturalRecovery(nutrientBonus:int)
    }

    class Parasite {
        +String name
        +int damage
    }

    GardenSimulationAPI --> SimulationEngine
    GardenSimulationAPI --> Garden
    GardenSimulationAPI --> LoggingService

    SimulationEngine --> EventBus
    SimulationEngine --> Garden

    GardenModule <|.. WateringSystem
    GardenModule <|.. ClimateSystem
    GardenModule <|.. PestControlSystem
    GardenModule <|.. FertilizerSystem

    WateringSystem --> Garden
    ClimateSystem --> Garden
    PestControlSystem --> Garden
    FertilizerSystem --> Garden
    PestControlSystem --> Parasite

    EventBus --> GardenModule
    LoggingService --> Garden

    Garden *-- GardenGrid
    Garden *-- PlantInstance
    GardenGrid *-- Plot
    Plot --> PlantInstance
    PlantType --> PlantInstance
```

---

## 4. Sequence Diagram — 24-Hour API Test Flow

Each API environment call publishes an event, then advances one simulated day via `tickHour()`.

```mermaid
sequenceDiagram
    participant Script
    participant API as GardenSimulationAPI
    participant Engine as SimulationEngine
    participant Bus as EventBus
    participant Module as GardenModule
    participant Garden
    participant Logger as LoggingService

    Script->>API: initializeGarden()
    API->>Garden: loadFromConfig(config, definitions)
    API->>Engine: start()
    Engine->>Logger: log(0, INIT, config_loaded, aliveCount)

    loop 24 simulated hours (= 24 days)
        Script->>API: rain / temperature / parasite
        alt Rain
            API->>Engine: onRain(amount)
            Engine->>Bus: publish(RAIN)
            Bus->>Module: onEvent(RAIN)
            Module->>Garden: apply water to plots
        else Temperature
            API->>Engine: onTemperature(temp)
            Engine->>Bus: publish(TEMPERATURE)
            Bus->>Module: onEvent(TEMPERATURE)
            Module->>Module: setTemperature(day, temp)
        else Parasite
            API->>Engine: onParasite(name)
            Engine->>Bus: publish(PARASITE)
            Bus->>Module: onEvent(PARASITE)
            Module->>Garden: apply stress to vulnerable plants
        end

        API->>Engine: tickHour()
        Engine->>Bus: notifyDayStart(day)
        Bus->>Module: onDayStart(day)
        Engine->>Garden: tickDay()
        Garden->>Garden: plot.tickDay() + removeDead()
        Engine->>Bus: notifyDayEnd(day)
        Bus->>Module: onDayEnd(day)
        Note over Module: sprinklers, thermal stress,<br/>fertilize low plots, tick infestations
        Engine->>Engine: incrementDay()
    end

    Script->>API: getState()
    API->>Logger: logState(day, garden)
    Note over Script,Logger: Final state written to log.txt;<br/>Script may also call getPlants()
```

---

## 5. Sequence Diagram — Parasite Handling

```mermaid
sequenceDiagram
    participant API as GardenSimulationAPI
    participant Engine as SimulationEngine
    participant Bus as EventBus
    participant Pest as PestControlSystem
    participant Garden
    participant Plant as PlantInstance
    participant Log as LoggingService

    API->>Engine: onParasite("aphid")
    Engine->>Bus: publish(PARASITE, aphid)
    Bus->>Pest: onEvent(PARASITE)
    Pest->>Garden: getLivingPlants()
    loop each plant
        Pest->>Plant: type.parasites contains aphid?
        alt vulnerable
            Pest->>Plant: applyStress(damage)
        end
    end
    Pest->>Log: log(day, PARASITE, aphid, alive)
    Pest->>Pest: deployControl(day, aphid)
    Note over Pest,Plant: partial recovery (+4 HP), not full heal
    Pest->>Plant: applyRecovery(CONTROL_RECOVERY)
    Pest->>Log: log(day, PEST_CONTROL, treated, alive)
    Note over Plant: further recovery via healRate on later ticks
```

---

## 6. Activity Diagram — Simulated Day Loop (tickHour)

Environment events (rain, temperature, parasite) are applied **before** `tickHour()` via `EventBus.publish()` when the API or GUI triggers them. Fertilizer runs automatically during `onDayEnd`.

```mermaid
flowchart TD
    A([tickHour start]) --> B[EventBus.notifyDayStart day]
    B --> C[Each module.onDayStart]
    C --> D[Garden.tickDay]
    D --> E[Each Plot.tickDay]
    E --> F[Each PlantInstance.tickDaily]
    F --> G[Remove plants with health <= 0]
    G --> H[Log PLANT_DEATH entries]
    H --> I[EventBus.notifyDayEnd day]
    I --> J[WateringSystem: sprinklers + moisture reset]
    I --> K[ClimateSystem: applyThermalStress + reset temp]
    I --> L[PestControlSystem: tickInfestations]
    I --> M[FertilizerSystem: fertilizeLowPlots]
    J --> N[SimulationClock.incrementDay]
    K --> N
    L --> N
    M --> N
    N --> O([tickHour end])
```

---

## 7. State Diagram — PlantInstance Lifecycle

Conceptual view aligned with `PlantInstance.updateStageFromHealth()`. New plants start in `GROWING` (not `SEEDLING`).

```mermaid
stateDiagram-v2
    [*] --> Growing: created (health=100)
    Growing --> Mature: health >= 80
    Mature --> Growing: health 60-79
    Growing --> Stressed: health 30-59
    Mature --> Stressed: drought / heat / pests
    Stressed --> Recovering: water OK + tickNaturalRecovery
    Recovering --> Mature: health >= 80
    Recovering --> Growing: health 60-79
    Stressed --> Dying: health < 30
    Dying --> Recovering: conditions improve
    Dying --> Dead: health <= 0
    Dead --> [*]
```

---

## 8. Deployment View (Logical)

```mermaid
flowchart LR
    subgraph JVM
        MainFX[GardenApp JavaFX]
        MainAPI[Headless Main / JUnit]
        Core[Shared Domain + Modules]
    end
    subgraph Filesystem
        ConfigJSON[config/garden_config.json]
        LogFile[log.txt]
        UserManual[docs/user-manual.md]
    end
    MainFX --> Core
    MainAPI --> Core
    Core --> ConfigJSON
    Core --> LogFile
    MainFX --> UserManual
```

---

## 9. Recommended Package Structure

```
com.csen275.garden
├── api/           GardenSimulationAPI
├── app/           GardenApp (JavaFX), HeadlessSimulationRunner
├── config/        ConfigLoader, GardenConfig
├── domain/
│   ├── garden/    Garden, GardenGrid, Plot
│   ├── plant/     PlantType, PlantInstance, GrowthStage
│   ├── insect/    Insect, Parasite
│   └── sensor/    Sensor, Sprinkler, Thermometer
├── module/        GardenModule, Watering, Climate, PestControl, Fertilizer
├── simulation/    SimulationEngine, SimulationClock, EventBus, EnvironmentEventGenerator
├── event/         GardenEvent, EventType
├── logging/       LoggingService
└── ui/            controllers, views, help
```

---

## 10. Design Decision Summary

| Decision | Rationale |
|----------|-----------|
| API and GUI share domain layer | Avoid duplicate logic; satisfy standalone API testing |
| `GardenModule` interface | Meets “≥3 independent modules” grading requirement |
| `EventBus` decoupling | Rain/climate/pest events routed to all modules without direct engine coupling |
| Events before `tickHour()` | API methods publish environment events, then advance the simulated day |
| `PlantType` vs `PlantInstance` | Type definition vs runtime instance; supports `getPlants()` |
| Automatic vs manual fertilizer | Low-nutrient plots fertilized in `onDayEnd`; GUI can trigger manual override |
| Relative paths for config/logs | Required by API specification |
| Pest control without instant full heal | `deployControl` applies partial recovery; full recovery is gradual via `healRate` |
