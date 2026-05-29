# CSEN275 Final Project — Computerized Garden Simulation

Automated garden simulation in Java (JavaFX + headless API).  
Course repo for analysis, design, implementation, and grading.

**Start here:** [`docs/04-team-execution-plan.md`](docs/04-team-execution-plan.md) — build steps in order (Step 0 → Step 16).

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Git

```bash
java -version
mvn -version
```

---

## Quick start

```bash
git clone https://github.com/Calvingo/CSEN275_final_project.git
cd CSEN275_final_project
git pull origin main
```

Follow **Step 0** in the execution plan to scaffold the Maven project, then continue in order.

---

## How to read the files in this repo

### Root

| File / folder | Purpose |
|---------------|---------|
| `README.md` | This file — project overview and where to look |
| `.gitignore` | Ignores build output (`target/`), `log.txt`, IDE files |
| `docs/` | All planning and course reference documents |

### `docs/` — read in this order

| Document | When to read | What it contains |
|----------|--------------|------------------|
| [`Requirements.pdf`](docs/Requirements.pdf) | First | **Official course handout** — grading rules, JavaFX requirement, ≥3 modules, logging, 24h run |
| [`Gardening System APIs.pdf`](docs/Gardening%20System%20APIs.pdf) | First | **Official API spec** — `GardenSimulationAPI` method signatures and test behavior |
| [`01-requirements.md`](docs/01-requirements.md) | Planning | Our written requirements: features, user stories, FR/NFR, acceptance criteria |
| [`02-uml-design.md`](docs/02-uml-design.md) | Planning / coding | UML diagrams: use case, class, sequence, activity, state, package structure |
| [`03-implementation-plan.md`](docs/03-implementation-plan.md) | Planning | Architecture overview, phases, config examples, tech stack |
| [`04-team-execution-plan.md`](docs/04-team-execution-plan.md) | **Daily work** | **Step-by-step build order**, tests per step, git workflow |

**Rule of thumb:**

- **Grading / “what must we build?”** → course PDFs + `01-requirements.md`
- **Classes and relationships** → `02-uml-design.md`
- **What to code today** → `04-team-execution-plan.md` (current step only)

---

## Git workflow (team)

```bash
git pull origin main
git checkout -b step-XX-short-description
# do the work for that step
mvn test
git add .
git commit -m "Step XX: short description"
git push -u origin step-XX-short-description
```

Open a **Pull Request** on GitHub → review → merge to `main`.  
Do not skip steps in the execution plan; later steps depend on earlier ones.

---

## Build and run (after Step 0 is done)

```bash
mvn test                  # unit / integration tests
mvn javafx:run            # GUI (after Step 13)
bash script/run-24h-test.sh   # headless 24h test (after Step 12)
```

---

## Project layout (after Step 0)

```
CSEN275_final_project/
├── README.md
├── pom.xml
├── config/
│   ├── garden_config.json
│   └── plant_definitions.json
├── docs/                 # planning & manuals
├── src/main/java/com/csen275/garden/
└── src/test/java/
```

---

## Questions?

1. Check the **current step** in `docs/04-team-execution-plan.md`
2. Cross-check course PDFs if behavior is unclear
3. Coordinate in team chat — avoid two people on the same step/file

Repository: https://github.com/Calvingo/CSEN275_final_project
