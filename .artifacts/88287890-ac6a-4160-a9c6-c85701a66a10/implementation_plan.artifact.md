# Implementation Plan - GridSurge Stability & Refactoring

This plan addresses several critical issues identified in the build, including crashes, logic bugs, and ergonomic improvements.

## User Review Required

> [!IMPORTANT]
> The touch ergonomic offset will be increased from 36dp to 60dp to prevent finger occlusion. This is a significant change to the feel of the game.

## Proposed Changes

### Core Engine & Stability

#### [MODIFY] [SpecialBlockSolver.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/game/engine/SpecialBlockSolver.kt)
- Update `evaluateSpecialClear` for `QUANTUM_WARP_VORTEX` to use radial targeting logic instead of a simple 3x3 square.
- Ensure strict boundary clamping to prevent `ArrayIndexOutOfBoundsException`.

#### [MODIFY] [GridSurgeGameView.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/game/GridSurgeGameView.kt)
- Update `completeCommitCycle` to maintain `maxSimultaneousLinesCleared` for star evaluation.
- Increase `liftOffset` in `updateDragPosition` to `60f * density` for better ergonomics.
- Ensure `renderSpecialHoverIndicator` handles the new radial targeting if necessary.

### Mission Objectives

#### [MODIFY] [StarRatingEvaluator.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/adventure/engine/StarRatingEvaluator.kt)
- Ensure the evaluator uses the persistent `maxSimultaneousLines` from the telemetry. (Already implemented, but depends on `GridSurgeGameView` updating it).

### Rogue-lite Systems

#### [MODIFY] [NeuralAugmentDraftManager.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/adventure/engine/NeuralAugmentDraftManager.kt)
- Implement deduplication in `rollAugmentDraft` to filter out already installed augments.

## Verification Plan

### Automated Tests
- N/A (Project lacks specific test suites for these components, manual verification on device is required).

### Manual Verification
- **Crash Test**: Drag a Warp Bomb to all four corners and edges of the grid. Verify no crash occurs.
- **Mastery Star Test**: Trigger a Double Blitz and verify the victory dialog correctly awards the Sector Mastery star.
- **Ergonomics Test**: Verify that the dragged piece is visible above the finger/thumb during drag.
- **Augment Test**: Reroll augments multiple times and verify that already installed augments do not appear in the draft.
