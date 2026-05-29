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
    UC5 -.-> Watering[Watering Subsystem]
    UC6 -.-> Climate[Climate Subsystem]
    UC7 -.-> Pest[Pest Control Subsystem]
```

---

## 2. Component Diagram

```mermaid
flowchart TB
    subgraph Presentation
        JavaFX[JavaFX UI]
        API[GardenSimulationAPI]
    end

    subgraph Application
        SimEngine[SimulationEngine]
        EventBus[EventBus]
    end

    subgraph Domain
        Garden[Garden / GardenGrid]
        Plants[Plant Hierarchy]
    end

    subgraph Subsystems
        Water[WateringSystem]
        Climate[ClimateSystem]
        PestCtrl[PestControlSystem]
    end

    subgraph Infrastructure
        Log[LoggingService]
        Config[ConfigLoader]
        Clock[SimulationClock]
    end

    JavaFX --> SimEngine
    JavaFX --> Garden
    API --> SimEngine
    API --> Garden
    API --> Log

    SimEngine --> Clock
    SimEngine --> EventBus
    EventBus --> Water
    EventBus --> Climate
    EventBus --> PestCtrl

    Water --> Garden
    Climate --> Garden
    PestCtrl --> Garden
    Garden --> Plants

    Config --> Garden
    Water --> Log
    Climate --> Log
    PestCtrl --> Log
    SimEngine --> Log
```

---

## 3. Class Diagram (Core)

```mermaid
classDiagram
    direction TB

    class GardenSimulationAPI {
        -SimulationEngine engine
        -Garden garden
        -LoggingService logger
        +initializeGarden()
        +getPlants() Map~String,Object~
        +rain(int amount)
        +temperature(int fahrenheit)
        +parasite(String name)
        +getState()
    }

    class SimulationEngine {
        -SimulationClock clock
        -EventBus eventBus
        -List~GardenModule~ modules
        +start()
        +tickHour()
        +onRain(int)
        +onTemperature(int)
        +onParasite(String)
    }

    class Garden {
        -GardenGrid grid
        -Map~String,PlantType~ plantTypes
        -List~PlantInstance~ livingPlants
        +loadFromConfig(Config)
        +getLivingCount() int
        +getLivingPlants() List
        +removeDead()
    }

    class GardenGrid {
        -int rows, cols
        -Plot[][] plots
        +getPlot(x,y) Plot
        +placePlant(PlantInstance, x, y)
    }

    class Plot {
        -PlantInstance plant
        -double soilMoisture
        -List~Sensor~ sensors
        +applyWater(int)
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
        +boolean isAlive
        +applyStress(int)
        +applyWater(int)
        +tickNaturalRecovery()
    }

    class GardenModule {
        <<interface>>
        +onDayStart(day)
        +onDayEnd(day)
        +onEvent(GardenEvent)
        +getName() String
    }

    class WateringSystem {
        -List~Sprinkler~ sprinklers
        -double rainBoost
        +activateSprinklers()
        +handleRain(int)
        +resetDailyMoisture()
    }

    class ClimateSystem {
        -int currentTempF
        -int defaultTempF
        +setTemperature(int)
        +resetDaily()
        +applyThermalStress(Garden)
    }

    class PestControlSystem {
        -Map~String,Infestation~ active
        +triggerParasite(String)
        +deployControl(String)
        +tickInfestations()
    }

    class LoggingService {
        -Path logPath
        +log(day, event, value, alive)
        +logState(Garden)
    }

    class EventBus {
        +publish(GardenEvent)
        +subscribe(GardenModule)
    }

    GardenSimulationAPI --> SimulationEngine
    GardenSimulationAPI --> Garden
    GardenSimulationAPI --> LoggingService
    SimulationEngine --> EventBus
    SimulationEngine --> GardenModule
    Garden --> GardenGrid
    Garden --> PlantType
    Garden --> PlantInstance
    GardenGrid --> Plot
    Plot --> PlantInstance
    PlantType --> PlantInstance
    WateringSystem ..|> GardenModule
    ClimateSystem ..|> GardenModule
    PestControlSystem ..|> GardenModule
    WateringSystem --> Garden
    ClimateSystem --> Garden
    PestControlSystem --> Garden
    EventBus --> GardenModule
```

---

## 4. Sequence Diagram — 24-Hour API Test Flow

```mermaid
sequenceDiagram
    autonumber
    participant Script
    participant API as GardenSimulationAPI
    participant Engine as SimulationEngine
    participant Garden
    participant Water as WateringSystem
    participant Log as LoggingService

    Script->>API: initializeGarden()
    API->>Garden: loadFromConfig(garden_config.json)
    API->>Engine: start() // Day 0 begins
    API->>Log: log(0, INIT, loaded, aliveCount)

    loop 24 simulated hours (= 24 days)
        Script->>API: rain/temperature/parasite (random)
        API->>Engine: dispatch event
        Engine->>Water: onEvent(...)
        Water->>Garden: update moisture / health
        Engine->>Garden: tickDay()
        Garden->>Garden: removeDead()
        Engine->>Log: log(day, EVENT, value, alive)
    end

    Script->>API: getState()
    API->>Garden: snapshot()
    API->>Log: logState(snapshot)
```

---

## 5. Sequence Diagram — Parasite Handling

```mermaid
sequenceDiagram
    participant API as GardenSimulationAPI
    participant Pest as PestControlSystem
    participant Garden
    participant Plant as PlantInstance
    participant Log as LoggingService

    API->>Pest: triggerParasite("aphid")
    Pest->>Garden: getLivingPlants()
    loop each plant
        Pest->>Plant: isVulnerable("aphid")?
        alt vulnerable
            Pest->>Plant: applyStress(damage)
            Pest->>Pest: recordInfestation(plant)
        end
    end
    Pest->>Pest: deployControl() // no instant heal
    Pest->>Log: log(day, PARASITE, aphid, alive)
    Note over Plant: gradual recovery via healRate on later ticks
```

---

## 6. Activity Diagram — Simulated Day Loop (tickHour)

```mermaid
flowchart TD
    Start([tickHour start]) --> IncDay[day++]
    IncDay --> DayStart[each Module.onDayStart]
    DayStart --> ProcessEnv{pending environment event?}
    ProcessEnv -->|rain| HandleRain[WateringSystem.handleRain]
    ProcessEnv -->|temperature| HandleTemp[ClimateSystem.setTemperature]
    ProcessEnv -->|parasite| HandlePest[PestControlSystem.trigger]
    ProcessEnv -->|none| ModuleTick
    HandleRain --> ModuleTick
    HandleTemp --> ModuleTick
    HandlePest --> ModuleTick
    ModuleTick[each Module daily logic] --> PlantTick[each Plant.tick + Plot.tick]
    PlantTick --> RemoveDead[remove plants with health <= 0]
    RemoveDead --> DayEnd[each Module.onDayEnd / reset daily state]
    DayEnd --> LogEvent[LoggingService.log]
    LogEvent --> End([end])
```

---

## 7. State Diagram — PlantInstance Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Seedling: created
    Seedling --> Growing: health > 60
    Growing --> Mature: sustained health
    Mature --> Stressed: drought / heat / pests
    Stressed --> Recovering: conditions improve + natural recovery
    Recovering --> Mature: health restored > 50
    Stressed --> Dying: health <= 20
    Dying --> Dead: health <= 0
    Dead --> [*]
    Growing --> Stressed: environment worsens
    Seedling --> Dead: extreme conditions
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
├── app/           GardenApp (JavaFX), HeadlessRunner
├── config/        ConfigLoader, GardenConfig
├── domain/
│   ├── garden/    Garden, GardenGrid, Plot
│   ├── plant/     PlantType, PlantInstance, GrowthStage
│   └── sensor/    Sensor, Sprinkler, Thermometer
├── module/        GardenModule, Watering, Climate, PestControl
├── simulation/    SimulationEngine, SimulationClock, EventBus
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
| `EventBus` decoupling | Rain/climate/pest extensible and testable |
| `PlantType` vs `PlantInstance` | Type definition vs runtime instance; supports `getPlants()` |
| Relative paths for config/logs | Required by API specification |
| Pest control without instant heal | Required by API spec; reflected in Stressed→Recovering state |
