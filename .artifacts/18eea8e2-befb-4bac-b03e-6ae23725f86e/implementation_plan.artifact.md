# Implementation Plan: Fix Premature Core Victory (Duplicate Hit Registration)

The goal is to eliminate the duplicate damage dealt to Guardian Cores in Adventure Mode. Currently, `adventureBoard.processLineClears` is called twice: once in `commitDrop` and once in `completeCommitCycle`. We will consolidate all Adventure-related move processing into `completeCommitCycle` to ensure it runs exactly once per move cycle.

## Proposed Changes

### [Component] Game View
#### [MODIFY] [GridSurgeGameView.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/game/GridSurgeGameView.kt)

1.  **Remove redundant Adventure sync and processing from `commitDrop`**:
    *   Delete the `if (isAdventureModeActive) { ... }` block that syncs the grid and calls `processLineClears` and `onMoveCommitted` before `completeCommitCycle`.
2.  **Ensure `onMoveCommitted` is called in `completeCommitCycle`**:
    *   Add the call to `adventureBoard.onMoveCommitted` inside the Adventure processing block of `completeCommitCycle`.
    *   This ensures that both standard drops and special drops (like Warp blocks) trigger the move-committed logic (turn counters, victory checks) at the end of their lifecycle.
3.  **Audit Warp Block paths**:
    *   Check if `handleWarpBlockDetonation` needs its own `onMoveCommitted` call or if it should be consolidated as well.

## Verification Plan

### Automated Tests
*   Run local unit tests for `AdventureBoardManager` if available to ensure `hitsRemaining` logic is still correct.
*   (If a test for `GridSurgeGameView` exists, verify no regressions in line clear scoring).

### Manual Verification
*   **Sector 1 Stage 01**: Verify that a single line clear on the central core changes it to `CRACKED` and leaves `hitsRemaining = 1`, without triggering victory.
*   **Sector 1 Stage 01**: Verify that a second line clear on the same core destroys it and triggers victory.
*   **Sector 2 Stage 10**: Verify that Amber Furnace turn counters still decrement correctly (as they rely on `onMoveCommitted`).
