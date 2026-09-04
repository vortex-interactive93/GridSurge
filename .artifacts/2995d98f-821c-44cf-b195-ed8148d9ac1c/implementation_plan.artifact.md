# Implementation Plan: Smart Piece Dimming, EMP Revive, 7-Day Uplink & QoL Polish

This plan implements Smart Piece Dimming, an EMP Surge Revive system, a 7-Day Daily Login protocol, and Adventure mode progress visualization.

## Proposed Changes

### 1. Game Engine & Tray Logic

#### [MODIFY] [GridSurgeGameView.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/game/GridSurgeGameView.kt)
- Add `canPieceFitOnGrid(shapeMatrix: Array<IntArray>, grid: IntArray): Boolean` as requested (adapter for `dockSpawner.canPieceFitOnBoard`).
- Update `drawDockShape` to render pieces with 35% alpha if they are unplaceable.
- Draw a muted red border (`#4DFF0055`) around unplaceable dock slots.
- Implement `deployEmpSurgeRevive()`:
    - Clear 3x3 center area of the grid.
    - Trigger `MEGA_BLITZ_BURST` VFX, chromatic glitch, and `SfxType.MEGA_BLITZ`.
    - Replenish tray with fresh pieces.
    - Track `hasUsedReviveThisRun`.
- Implement `rerollTrayModules()` to refresh dock shapes.
- Expose `hasUsedReviveThisRun` and `starBalance` (from profile manager) to the UI.

### 2. UI Components

#### [NEW] [MatrixFailureReviveDialog.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/ui/components/MatrixFailureReviveDialog.kt)
- Implement the "CRITICAL FAILURE" dialog with EMP Surge revive option and Adventure progress bar.
- Use `CyberChamferShape` and custom button styles as specified.

#### [NEW] [DailyLoginDialog.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/ui/screens/DailyLoginDialog.kt)
- Implement the 7-day protocol UI using a grid layout.
- Rewards: Stars from 50 to 500.

#### [MODIFY] [GameScreen.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/ui/GameScreen.kt)
- Integrate `MatrixFailureReviveDialog` when the game is over.
- Pass star balance from `profileManager` to the revive dialog.
- Wire up the `onDeployEmp` callback to `gameViewRef?.deployEmpSurgeRevive()`.

### 3. Data & Persistence

#### [NEW] [DailyLoginRepository.kt](file:///C:/Users/Thomas/StudioProjects/GridSurge/app/src/main/java/com/example/gridsurge/meta/data/DailyLoginRepository.kt)
- Track `lastLoginEpochDay` and `consecutiveDaysStreak` in DataStore.
- Handle streak logic: reset to 1 if a day is skipped, cycle back to Day 1 after Day 7.
- Award stars via `PlayerProfileManager`.

## Verification Plan

### Automated Tests
- N/A (UI and Game Logic primarily verified visually)

### Manual Verification
1. **Dimming**: Fill the grid until some tray pieces cannot be placed. Verify they dim and show a red border.
2. **EMP Revive**: When game over occurs, use the "DEPLOY EMP SURGE" button. Verify the 3x3 center clears, VFX/SFX trigger, and pieces refresh.
3. **Daily Login**: Launch the app and verify the daily login dialog appears (or can be triggered). Claim a reward and check the star balance increase.
4. **Adventure Progress**: In Adventure mode, intentionally lose and verify the progress bar shows the correct percentage of cores neutralized.
