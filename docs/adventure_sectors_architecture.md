# Adventure Mode Architecture & File Overview

This document summarizes the current directory structure, file organization, and sector management design for GridSurge's Adventure Mode.

## Directory Location
All Adventure mode data registry and catalog files are located under:
`app/src/main/java/com/example/gridsurge/features/adventure/data/`

---

## Architectural Overview: Isolated vs. Centralized

The codebase uses a **hybrid architecture** that is transitioning toward **isolated per-sector registries**:

1. **Per-Sector Isolated Registries:**
   * **`Sector02BlueprintRegistry.kt`**: Contains handcrafted level blueprints specifically for **Sector 2** (Stages 10–18).
   * **`Sector02BenchmarkRegistry.kt`**: Defines star benchmarks, time/move limits, and mastery feats for **Sector 2**.
   * **`Sector01BenchmarkRegistry.kt`**: Defines star benchmarks, time/move limits, and mastery feats for **Sector 1**.

2. **Central Registry / Router:**
   * **`AdventureSectorRegistry.kt`**: Serves as the central registry hub (`val SECTORS = listOf(SECTOR_1, SECTOR_2)`). It currently holds Sector 1 blueprints inline (`SECTOR_01_BLUEPRINTS`) and routes level lookups for levels 10–18 to `Sector02BlueprintRegistry`.

3. **Catalog & Presentation Layer:**
   * **`AdventureCatalog.kt`**: Aggregates sector data from `AdventureSectorRegistry` into UI-ready models (`SectorSpec` and `LevelNodeSpec`) for map screens.
   * **`AdventureLevel.kt`**: Contains `CampaignCatalog`, an older/fallback static data list for Sectors 1 through 5.

4. **Procedural Generation Fallback:**
   * **`ProceduralStageGenerator.kt`** (`features/adventure/core/`): Generates dynamic, deterministic blueprints for any arbitrary sector number (e.g., Sectors 3+) when explicit handcrafted registries are not provided.

---

## Data Files Breakdown

| File Name | Location | Primary Responsibility | Scope / Isolation |
| :--- | :--- | :--- | :--- |
| `AdventureSectorRegistry.kt` | `features/adventure/data/` | Central registry hub; holds Sector 1 blueprints inline and routes to Sector 2. | **Central Hub + Sector 1** |
| `Sector01BenchmarkRegistry.kt` | `features/adventure/data/` | Benchmark time/moves and mastery feat goals for Sector 1. | **Isolated (Sector 1)** |
| `Sector02BlueprintRegistry.kt` | `features/adventure/data/` | Handcrafted stage layout blueprints for Sector 2 (Stages 10–18). | **Isolated (Sector 2)** |
| `Sector02BenchmarkRegistry.kt` | `features/adventure/data/` | Benchmark time/moves and mastery feat goals for Sector 2. | **Isolated (Sector 2)** |
| `AdventureCatalog.kt` | `features/adventure/data/` | Transforms sector definitions into UI map presentation models. | **Central Presentation** |
| `AdventureLevel.kt` | `features/adventure/data/` | Defines `CampaignCatalog` static sector data (Sectors 1–5 fallback). | **Legacy / Fallback** |
| `ProceduralStageGenerator.kt` | `features/adventure/core/` | Procedural level generator using deterministic seeds per sector/stage. | **Global Generator** |

---

## Recommended Next Steps / Best Practices for New Sectors

When adding a new sector (e.g., Sector 3):
1. **Create isolated sector files**:
   * `Sector03BlueprintRegistry.kt` (containing stages 19–27)
   * `Sector03BenchmarkRegistry.kt`
2. **Register in `AdventureSectorRegistry.kt`**:
   * Add `SECTOR_3` metadata definition to `AdventureSectorRegistry.SECTORS`.
   * Update `getLevelBlueprint()` to delegate levels 19–27 to `Sector03BlueprintRegistry`.
3. (Optional refactor) Extract Sector 1 blueprints out of `AdventureSectorRegistry.kt` into an isolated `Sector01BlueprintRegistry.kt` for full consistency.
