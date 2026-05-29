# CSEN275 Computerized Garden System — Implementation Plan

## 1. Overall Architecture

**Layered + modular** architecture:

```
┌─────────────────────────────────────────┐
│  Presentation: JavaFX UI | GardenAPI    │
├─────────────────────────────────────────┤
│  Application: SimulationEngine, EventBus│
├─────────────────────────────────────────┤
│  Modules: Water | Climate | PestControl │
├─────────────────────────────────────────┤
│  Domain: Garden, Plant, Sensor, Insect   │
├─────────────────────────────────────────┤
│  Infrastructure: Log, Config (relative)  │
└─────────────────────────────────────────┘
```

**Key principle**: `GardenSimulationAPI` and JavaFX depend on the same domain/modules—no duplicated business logic.

---

## 2. Suggested Team Split (3 members)

| Member | Owns | Deliverables |
|--------|------|--------------|
| A | Domain layer + Config + Logging | Garden, Plant, log.txt format |
| B | Three subsystems + SimulationEngine | Water/Climate/Pest + tick |
| C | GardenSimulationAPI + JavaFX UI | API signatures, UI, User Manual |

---

## 3. Implementation Phases

### Phase 1 — Domain Model (Week 1)
- [ ] `PlantType`, `PlantInstance`, `Garden`, `GardenGrid`, `Plot`
- [ ] `config/garden_config.json` + `ConfigLoader`
- [ ] `LoggingService` → fixed `log.txt` format
- [ ] Unit tests: config load, dead plant removal

### Phase 2 — Subsystems (Week 2)
- [ ] `GardenModule` interface + `EventBus`
- [ ] `WateringSystem` (sprinklers, rain, end-of-day reset)
- [ ] `ClimateSystem` (40–120°F, end-of-day reset)
- [ ] `PestControlSystem` (parasite lists, control without instant heal)
- [ ] `SimulationEngine.tickHour()`

### Phase 3 — API (Week 3)
- [ ] Implement all `GardenSimulationAPI` methods
- [ ] Headless integration test: 24-hour loop per course PDF pseudocode
- [ ] Global `try/catch` + error logging; process must not exit

### Phase 4 — JavaFX (Week 3–4)
- [ ] Grid view, plant status colors, subsystem panels
- [ ] Add-plant dialog (name, water, parasites)
- [ ] Log viewer tab + Help
- [ ] Bind UI to engine (optional accelerated tick)

### Phase 5 — Endurance and Documentation (Week 4)
- [ ] Local 24h+ run script
- [ ] User Manual, Log Guide
- [ ] Class presentation (<10 min)

---

## 4. Configuration Examples

**config/garden_config.json**
```json
{
  "plants": [
    { "name": "Rose", "amount": 2 },
    { "name": "Tomato", "amount": 2 },
    { "name": "Sunflower", "amount": 1 },
    { "name": "Basil", "amount": 1 },
    { "name": "Lavender", "amount": 1 },
    { "name": "Marigold", "amount": 1 },
    { "name": "Lettuce", "amount": 1 },
    { "name": "Pepper", "amount": 1 },
    { "name": "Daisy", "amount": 1 },
    { "name": "OakSapling", "amount": 1 }
  ]
}
```

**config/plant_definitions.json** (internal extension for type attributes)
```json
{
  "Rose": { "waterRequirement": 10, "parasites": ["aphid", "spider_mite"] },
  "Tomato": { "waterRequirement": 15, "parasites": ["aphid", "hornworm"] }
}
```

---

## 5. Log Format Specification

```
DAY, EVENT, EVENT_VALUE, PLANTS_ALIVE
```

Example `log.txt`:
```
0, INIT, garden_config.json, 12
1, RAIN, 12, 12
2, TEMPERATURE, 95, 12
3, PARASITE, aphid, 11
3, PLANT_DEATH, Rose@1-2, 11
...
23, STATE, alive=8 dead=4, 8
```

Also provide `docs/log-guide.md` documenting each EVENT type.

---

## 6. API Test Pseudocode (aligned with course PDF)

```java
GardenSimulationAPI api = new GardenSimulationAPI();
api.initializeGarden();

Random rng = new Random();
for (int hour = 1; hour <= 24; hour++) {
    int choice = rng.nextInt(3);
    switch (choice) {
        case 0 -> api.rain(randomRainAmount(api));
        case 1 -> api.temperature(40 + rng.nextInt(81)); // 40-120
        case 2 -> api.parasite(randomParasite(api));
    }
    // drive tickHour() internally or externally
}
api.getState();
```

---

## 7. Technology Choices

| Item | Choice |
|------|--------|
| Java | 17+ |
| UI | JavaFX 21 + FXML (or programmatic layout) |
| Build | Maven |
| Testing | JUnit 5 |
| JSON | Jackson or Gson |

---

## 8. Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| 24h crash | Wrap every module tick in try/catch; isolate per-plant failures |
| Unreadable logs | Single LoggingService; no scattered `System.out` |
| All dead / never die | Parameterize stress/heal; integration tests for intermediate states |
| GUI vs API mismatch | Shared SimulationEngine via injection or singleton |
| Plagiarism | Cite gardening references in documentation |

---

## 9. Course Deliverable Checklist

- [ ] Requirements analysis (`docs/01-requirements.md`)
- [ ] OO design UML (`docs/02-uml-design.md`)
- [ ] Java implementation + JavaFX
- [ ] Standalone `GardenSimulationAPI`
- [ ] `log.txt` + log documentation
- [ ] User Manual / Help
- [ ] Short class presentation
