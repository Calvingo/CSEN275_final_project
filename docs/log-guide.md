# Log Guide — Garden Simulation

## File

All events are written to `log.txt` in the project root (relative path).  
Each line is appended — the file is never overwritten during a run.

## Format

```
DAY, EVENT, EVENT_VALUE, PLANTS_ALIVE
```

| Field         | Type    | Description                                      |
|---------------|---------|--------------------------------------------------|
| DAY           | int     | Simulated day number (starts at 0)               |
| EVENT         | string  | Event type (see table below)                     |
| EVENT_VALUE   | string  | Event-specific detail                            |
| PLANTS_ALIVE  | int     | Number of living plants at time of log entry     |

## Event Types

| EVENT        | When written                                      | EVENT_VALUE example                     |
|--------------|---------------------------------------------------|-----------------------------------------|
| INIT         | Garden initialized via `initializeGarden()`       | `config_loaded`                         |
| RAIN         | `rain(int)` called                                | `25`                                    |
| TEMPERATURE  | `temperature(int)` called                         | `105`                                   |
| PARASITE     | `parasite(String)` called                         | `aphid`                                 |
| FERTILIZER | FertilizerSystem treats low-nutrient plots | `plot(2,1) nutrients=45` |
| NUTRIENT_CHECK | Many plots below nutrient threshold | `low_plots=6` |
| MANUAL_FERTILIZER | User applies fertilizer from GUI | `user_triggered` |
| CLIMATE_CONTROL | Heating or cooling activated for extreme temperature | `cooling_active temp=105` |
| PLANT_DEATH  | A plant's health drops to 0                       | `Rose@plot(1,2)`                        |
| SPRINKLER    | Sprinkler activated by WateringSystem             | `plot(0,0) water=15`                    |
| STATE        | `getState()` called                               | `alive=8 plants=[Rose(health=72) ...]`  |
| ERROR        | A subsystem throws a caught exception             | `WateringSystem: NullPointerException`  |
| DAY_START    | Start of each simulated day tick                  | `day=3`                                 |
| DAY_END      | End of each simulated day tick                    | `day=3`                                 |
| MANUAL_WATER | User triggers manual watering from GUI            | `plot(2,1)`                             |

## Example log.txt

```
0, INIT, config_loaded, 12
0, STATE, alive=12 plants=[Rose(health=100) Tomato(health=100)], 12
1, RAIN, 25, 12
1, DAY_END, day=1, 12
2, TEMPERATURE, 105, 12
2, DAY_END, day=2, 11
3, PARASITE, aphid, 11
3, PLANT_DEATH, Rose@plot(1,2), 10
3, DAY_END, day=3, 10
```
